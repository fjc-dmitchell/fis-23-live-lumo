package gov.fjc.fis.job;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import gov.fjc.fis.entity.dto.EmployeeDto;
import gov.fjc.fis.entity.personnel.Employee;
import gov.fjc.fis.service.AdministrationService;
import io.jmix.core.SaveContext;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.email.*;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@DisallowConcurrentExecution
public class LoadEmployeesJob implements Job {
    @Autowired
    private AdministrationService administrationService;
    @Autowired
    private UnconstrainedDataManager unconstrainedDataManager;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    private Emailer emailer;
    @Autowired
    Scheduler scheduler;

    @Value("${hrmis.feed.directory}")
    private String feedDirectory;
    @Value("${hrmis.archive.directory}")
    private String archiveDirectory;
    @Value("${hrmis.file}")
    private String employeeFile;
    @Value("${hrmis.email.job-status.addresses}")
    private String jobStatusEmailAddresses;

    private static final JobKey SYNC_EMPLOYEES_JOB_KEY = new JobKey("syncEmployeeToPosition", "HRMIS");
    private static final String ABEND_SUBJECT = "HRMIS feed processing ABENDED";
    private static final Logger log = LoggerFactory.getLogger(LoadEmployeesJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("LoadEmployeesJob has been executed.");
        var employeeFilePath = Path.of(feedDirectory, employeeFile);

        if (!Files.exists(employeeFilePath)) {
            notifyFileMissing(employeeFilePath);
            throw new JobExecutionException(ABEND_SUBJECT);
        }

        try {
            jdbcTemplate.execute("TRUNCATE TABLE FIS_EMPLOYEE");
            loadEmployees(employeeFilePath);
            administrationService.archiveFile(jobStatusEmailAddresses, ABEND_SUBJECT,
                    feedDirectory, archiveDirectory, employeeFile);
            scheduler.triggerJob(SYNC_EMPLOYEES_JOB_KEY);
            log.info("LoadEmployees completed");
        } catch (SchedulerException e) {
            notifyFailure("<strong>LoadEmployees</strong> failed to trigger <strong>syncEmployeeToPosition</strong>");
            throw new JobExecutionException(ABEND_SUBJECT, e);
        } catch (Exception e) {
            log.error("Employee load failed after truncating FIS_EMPLOYEE", e);
            notifyFailure("<strong>LoadEmployees</strong> failed while loading employees: " + e.getMessage());
            throw new JobExecutionException(ABEND_SUBJECT, e);
        }
    }

    private void notifyFileMissing(Path employeeFilePath) {
        log.warn("{} not found at {}. Processing aborted.", employeeFile, employeeFilePath);
        var body = """
                <strong>%s</strong> could not be found at:<br /><br />
                %s<br /><br />
                <strong>Employees are untouched. Processing has been aborted.</strong>
                """.formatted(employeeFile, employeeFilePath);
        administrationService.sendEmail(jobStatusEmailAddresses, ABEND_SUBJECT, body);
    }

    private void notifyFailure(String message) {
        administrationService.sendEmail(jobStatusEmailAddresses, ABEND_SUBJECT, message);
    }

    private void loadEmployees(Path employeeFilePath) {
        try (Reader reader = Files.newBufferedReader(employeeFilePath)) {
            CsvToBean<EmployeeDto> csvToBean = new CsvToBeanBuilder<EmployeeDto>(reader)
                    .withType(EmployeeDto.class)
                    .withSeparator('\t')
                    .withIgnoreLeadingWhiteSpace(true)
                    .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS)
                    .build();

            List<EmployeeDto> employees = csvToBean.parse();

            SaveContext saveContext = new SaveContext();

            for (var employeeDto : employees) {
                Employee employee = createEmployee(employeeDto);
                saveContext.saving(employee);
            }

            unconstrainedDataManager.save(saveContext);
            log.info("Imported employees: {}", employees.size());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    Employee createEmployee(EmployeeDto employeeDto) {
        Employee employee = unconstrainedDataManager.create(Employee.class);
        employee.setPositionNbr(employeeDto.getPositionNbr());
        employee.setEmplid(employeeDto.getEmplid());
        employee.setName(employeeDto.getName());
        employee.setDeptId(employeeDto.getDeptId());
        employee.setJobcode(employeeDto.getJobcode());
        employee.setFullPartTime(employeeDto.getFullPartTime());
        employee.setRegTemp(employeeDto.getRegTemp());
        employee.setPaygroup(employeeDto.getPaygroup());
        employee.setGrade(employeeDto.getGrade());
        employee.setStep(employeeDto.getStep());
        employee.setEmplType(employeeDto.getEmplType());
        employee.setStdHours(employeeDto.getStdHours());
        employee.setJobtitle(employeeDto.getJobtitle());
        employee.setHourlyRt(employeeDto.getHourlyRt());
        employee.setGvtBiweeklyRt(employeeDto.getGvtBiweeklyRt());
        employee.setAnnualRt(employeeDto.getAnnualRt());
        employee.setGvtApptExpirDt(employeeDto.getGvtApptExpirDt());
        employee.setJlBudCatgCd(employeeDto.getJlBudCatgCd());
        employee.setJlCostOrgCd(employeeDto.getJlCostOrgCd());
        employee.setGvtComprate(employeeDto.getGvtComprate());
        employee.setGvtLocalityAdj(employeeDto.getGvtLocalityAdj());
        employee.setGvtWorkSched(employeeDto.getGvtWorkSched());
        return employee;
    }
}
