package gov.fjc.fis.job.hrmis;

import gov.fjc.fis.entity.personnel.Employee;
import gov.fjc.fis.entity.personnel.Position;
import gov.fjc.fis.entity.personnel.PositionAudit;
import io.jmix.core.SaveContext;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.security.Authenticated;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@DisallowConcurrentExecution
public class SyncEmployeeToPositionJob implements Job {

    @Autowired
    private UnconstrainedDataManager dataManager;

    private static final Logger log = LoggerFactory.getLogger(SyncEmployeeToPositionJob.class);

    // mutable instance variable! Normally bad, but ok since we DisallowConcurrentExecution
    private OffsetDateTime runTimestamp;

    // special case for intermittent employees
    private static final String STD_HOURS_LABEL = "Std. Hours";
    private static final String ANNUAL_RT_LABEL = "Annual Rate";

    // any employee present in the HRMIS feed implies an active position
    private static final Function<Employee, String> ALWAYS_ACTIVE = e -> "A";

    private record FieldMapping<V>(
            String fieldName,
            Function<Employee, V> employeeGetter,
            Function<Position, V> positionGetter,
            BiConsumer<Position, V> positionSetter) {
    }

    private static final List<FieldMapping<?>> FIELD_MAPPINGS = List.of(
            new FieldMapping<>("Name", Employee::getName,
                    Position::getName, Position::setName),
            new FieldMapping<>("emplid", Employee::getEmplid,
                    Position::getEmplid, Position::setEmplid),
            new FieldMapping<>("Job Title", Employee::getJobtitle,
                    Position::getJobtitle, Position::setJobtitle),
            new FieldMapping<>("Regular/Temp", Employee::getRegTemp,
                    Position::getRegTemp, Position::setRegTemp),
            // refactor paygroup to setId
            new FieldMapping<>("Pay Group", Employee::getPaygroup,
                    Position::getPaygroup, Position::setPaygroup),
            new FieldMapping<>("NTE date", Employee::getGvtApptExpirDt,
                    Position::getGvtApptExpirDt, Position::setGvtApptExpirDt),
            new FieldMapping<>("Cost Org.", Employee::getJlCostOrgCd,
                    Position::getJlCostOrgCd, Position::setJlCostOrgCd),
            new FieldMapping<>("Grade", Employee::getGrade,
                    Position::getGrade, Position::setGrade),
            new FieldMapping<>(STD_HOURS_LABEL, Employee::getStdHours,
                    Position::getStdHours, Position::setStdHours),
            new FieldMapping<>("Hourly Rate", Employee::getHourlyRt,
                    Position::getHourlyRt, Position::setHourlyRt),
            new FieldMapping<>(ANNUAL_RT_LABEL, Employee::getAnnualRt,
                    Position::getAnnualRt, Position::setAnnualRt),
            new FieldMapping<>("Salary", Employee::getTotalPay,
                    Position::getTotalPay, Position::setTotalPay),
            new FieldMapping<>("Work Sched.", Employee::getGvtWorkSched,
                    Position::getGvtWorkSched, Position::setGvtWorkSched),
            new FieldMapping<>("Status", ALWAYS_ACTIVE,
                    Position::getStatus, Position::setStatus)
    );

    @Authenticated
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("SyncEmployeeToPosition starting");

        try {
            List<Employee> employees = fetchAllEmployees();
            Map<String, Position> positionsByNbr = fetchAllPositions();
            List<PositionAudit> auditEntries = new ArrayList<>();
            runTimestamp = OffsetDateTime.now();

            SaveContext saveContext = new SaveContext();

            for (Employee employee : employees) {
                String positionNbr = employee.getPositionNbr();

                if (positionNbr == null || positionNbr.isBlank()) {
                    log.warn("Employee {} has no position number, skipping", employee.getEmplid());
                    continue;
                }

                Position existingPosition = positionsByNbr.get(positionNbr);
                if (existingPosition == null) {
                    Position created = createPositionFromEmployee(employee, positionNbr, auditEntries);
                    saveContext.saving(created);
                } else if (applyChanges(employee, existingPosition, auditEntries)) {
                    saveContext.saving(existingPosition);
                }
            }

            if (!saveContext.getEntitiesToSave().isEmpty()) {
                auditEntries.forEach(saveContext::saving);
                dataManager.save(saveContext);
            }

            log.info("SyncEmployeeToPosition completed: {} employees processed, {} entities saved",
                    employees.size(), saveContext.getEntitiesToSave().size());

        } catch (Exception e) {
            log.error("SyncEmployeeToPosition failed", e);
            JobExecutionException jobEx = new JobExecutionException("Employee-to-position sync failed", e);
            jobEx.setRefireImmediately(false);
            throw jobEx;
        }
    }

    private List<Employee> fetchAllEmployees() {
        return dataManager.load(Employee.class)
                .all()
                .list();
    }

    private Map<String, Position> fetchAllPositions() {
        return dataManager.load(Position.class)
                .all()
                .list()
                .stream()
                .collect(Collectors.toMap(Position::getPositionNbr, Function.identity(),
                        (a, b) -> {
                            log.warn("Duplicate positionNbr {}", a.getPositionNbr());
                            return a;
                        }));
    }

    private Position createPositionFromEmployee(Employee employee, String positionNbr,
                                                List<PositionAudit> auditEntries) {
        Position position = dataManager.create(Position.class);
        position.setPositionNbr(positionNbr);
        FIELD_MAPPINGS.forEach(mapping -> setField(employee, position, mapping));
        auditEntries.add(buildCreationAudit(position));
        return position;
    }

    private <V> void setField(Employee employee, Position position, FieldMapping<V> mapping) {
        mapping.positionSetter().accept(position, mapping.employeeGetter().apply(employee));
    }

    private boolean applyChanges(Employee employee, Position position, List<PositionAudit> auditEntries) {
        boolean changed = false;
        for (FieldMapping<?> mapping : FIELD_MAPPINGS) {
            changed |= applyIfChanged(employee, position, mapping, auditEntries);
        }
        return changed;
    }

    private <V> boolean applyIfChanged(Employee employee, Position position,
                                       FieldMapping<V> mapping, List<PositionAudit> auditEntries) {
        V oldValue = mapping.positionGetter().apply(position);
        V newValue = mapping.employeeGetter().apply(employee);

        // don't set stdHours or annualRt for employees with Intermittent work schedule
        if ((STD_HOURS_LABEL.equals(mapping.fieldName()) || ANNUAL_RT_LABEL.equals(mapping.fieldName()))
                && "I".equalsIgnoreCase(position.getGvtWorkSched())) {
            return false;
        }
        if (valuesEqual(oldValue, newValue)) {
            return false;
        }
        mapping.positionSetter().accept(position, newValue);
        auditEntries.add(buildFieldChangeAudit(position, mapping.fieldName(), oldValue, newValue));
        return true;
    }

    private boolean valuesEqual(Object oldValue, Object newValue) {
        // the scale is 2 in Employee, 0 in Position, so use compareTo
        if (oldValue instanceof BigDecimal oldBd && newValue instanceof BigDecimal newBd) {
            return oldBd.compareTo(newBd) == 0;
        }
        return Objects.equals(oldValue, newValue);
    }

    private PositionAudit buildCreationAudit(Position position) {
        PositionAudit audit = dataManager.create(PositionAudit.class);
        audit.setPositionNbr(position.getPositionNbr());
        audit.setName(position.getName());
        audit.setFieldName("CREATED");
        audit.setOldValue(null);
        audit.setNewValue("Position record created from HRMIS employee");
        audit.setChangedDate(runTimestamp);
        return audit;
    }

    private PositionAudit buildFieldChangeAudit(Position position, String fieldName,
                                                Object oldValue, Object newValue) {
        PositionAudit audit = dataManager.create(PositionAudit.class);
        audit.setPositionNbr(position.getPositionNbr());
        audit.setName(position.getName());
        audit.setFieldName(fieldName);
        audit.setOldValue(oldValue == null ? null : oldValue.toString());
        audit.setNewValue(newValue == null ? null : newValue.toString());
        audit.setChangedDate(runTimestamp);
        return audit;
    }
}
