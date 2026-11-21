package gov.fjc.fis.job.hrmis;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import gov.fjc.fis.entity.personnel.Employee;
import gov.fjc.fis.service.AdministrationService;
import io.jmix.core.SaveContext;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.security.Authenticated;
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

/**
 * Quartz job that imports Employee records from the HRMIS employee feed.
 * <p>
 * The job reads the configured tab-delimited employee file, clears existing
 * {@link gov.fjc.fis.entity.personnel.Employee} records, and loads the latest
 * employee data into FIS. The imported data serves as the authoritative
 * personnel source received from HRMIS and is subsequently used for position
 * synchronization, compensation projections, and workforce planning.
 * </p>
 *
 * <p>
 * After a successful import, the source file is archived and the
 * {@code syncEmployeeToPosition} job is triggered to synchronize employee
 * information with Position records. If the feed file is missing or an error
 * occurs during processing, the job sends a notification email and aborts
 * processing.
 * </p>
 *
 * <p>
 * Concurrent execution is disabled to prevent multiple imports from modifying
 * employee data simultaneously.
 * </p>
 *
 * @see gov.fjc.fis.entity.personnel.Employee
 * @see SyncEmployeeToPositionJob
 *
 * @author Doug Mitchell
 */
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

    /**
     * Executes the employee import process.
     * <p>
     * The job verifies that the configured HRMIS employee feed file exists before
     * replacing the current Employee data. It truncates the employee table, imports
     * and persists the records from the feed, archives the processed file, and
     * triggers the job that synchronizes Employee entities with Position records.
     * </p>
     *
     * <p>
     * If the feed file is missing, the existing Employee records remain unchanged.
     * If the import or downstream job trigger fails, a failure notification is sent
     * and execution terminates with a {@link JobExecutionException}.
     * </p>
     *
     * @param context the Quartz execution context for this job
     * @throws JobExecutionException if the feed file is missing, employee loading
     *                               fails, the file cannot be archived, or the
     *                               employee-to-position synchronization job cannot
     *                               be triggered
     */
    @Authenticated
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("LoadEmployeesJob has started");
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
        } catch (SchedulerException e) {
            notifyFailure("<strong>LoadEmployees</strong> failed to trigger <strong>syncEmployeeToPosition</strong>");
            throw new JobExecutionException(ABEND_SUBJECT, e);
        } catch (Exception e) {
            log.error("Employee load failed after truncating FIS_EMPLOYEE", e);
            notifyFailure("<strong>LoadEmployees</strong> failed while loading employees: " + e.getMessage());
            throw new JobExecutionException(ABEND_SUBJECT, e);
        }
        log.info("LoadEmployeesJob has ended");
    }

    /**
     * Reports that the configured HRMIS employee feed file could not be found.
     * <p>
     * A warning is written to the application log and an email notification is
     * sent indicating that processing was aborted without modifying the existing
     * Employee records.
     * </p>
     *
     * @param employeeFilePath the expected path of the missing employee feed file
     */
    private void notifyFileMissing(Path employeeFilePath) {
        log.warn("{} not found at {}. Processing aborted.", employeeFile, employeeFilePath);
        var body = """
                <strong>%s</strong> could not be found at:<br /><br />
                %s<br /><br />
                <strong>Employees are untouched. Processing has been aborted.</strong>
                """.formatted(employeeFile, employeeFilePath);
        administrationService.sendEmail(jobStatusEmailAddresses, ABEND_SUBJECT, body);
    }

    /**
     * Sends an email notification describing a failure encountered while processing
     * the HRMIS employee feed.
     *
     * @param message the HTML-formatted failure message to include in the email
     */
    private void notifyFailure(String message) {
        administrationService.sendEmail(jobStatusEmailAddresses, ABEND_SUBJECT, message);
    }

    /**
     * Loads employee records from the specified HRMIS feed file and persists
     * them in a batch operation.
     *
     * @param employeeFilePath the path to the employee feed file
     */
    private void loadEmployees(Path employeeFilePath) {
        try (Reader reader = Files.newBufferedReader(employeeFilePath)) {
            CsvToBean<EmployeeImportRow> csvToBean = new CsvToBeanBuilder<EmployeeImportRow>(reader)
                    .withType(EmployeeImportRow.class)
                    .withSeparator('\t')
                    .withIgnoreLeadingWhiteSpace(true)
                    .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS)
                    .build();

            List<EmployeeImportRow> employeeImportRows = csvToBean.parse();

            SaveContext saveContext = new SaveContext();

            for (var employeeImportRow : employeeImportRows) {
                Employee employee = createEmployee(employeeImportRow);
                saveContext.saving(employee);
            }

            unconstrainedDataManager.save(saveContext);
            log.info("Imported employees: {}", employeeImportRows.size());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates an Employee entity from a row in the HRMIS employee feed.
     *
     * @param employeeImportRow the imported employee feed record
     * @return a populated Employee entity that has not yet been persisted
     */
    Employee createEmployee(EmployeeImportRow employeeImportRow) {
        Employee employee = unconstrainedDataManager.create(Employee.class);
        employee.setPositionNbr(employeeImportRow.getPositionNbr());
        employee.setEmplid(employeeImportRow.getEmplid());
        employee.setName(employeeImportRow.getName());
        employee.setDeptId(employeeImportRow.getDeptId());
        employee.setJobcode(employeeImportRow.getJobcode());
        employee.setFullPartTime(employeeImportRow.getFullPartTime());
        employee.setRegTemp(employeeImportRow.getRegTemp());
        employee.setPaygroup(employeeImportRow.getPaygroup());
        employee.setGrade(employeeImportRow.getGrade());
        employee.setStep(employeeImportRow.getStep());
        employee.setEmplType(employeeImportRow.getEmplType());
        employee.setStdHours(employeeImportRow.getStdHours());
        employee.setJobtitle(employeeImportRow.getJobtitle());
        employee.setHourlyRt(employeeImportRow.getHourlyRt());
        employee.setGvtBiweeklyRt(employeeImportRow.getGvtBiweeklyRt());
        employee.setAnnualRt(employeeImportRow.getAnnualRt());
        employee.setGvtApptExpirDt(employeeImportRow.getGvtApptExpirDt());
        employee.setJlBudCatgCd(employeeImportRow.getJlBudCatgCd());
        employee.setJlCostOrgCd(employeeImportRow.getJlCostOrgCd());
        employee.setGvtComprate(employeeImportRow.getGvtComprate());
        employee.setGvtLocalityAdj(employeeImportRow.getGvtLocalityAdj());
        employee.setGvtWorkSched(employeeImportRow.getGvtWorkSched());
        return employee;
    }
}
