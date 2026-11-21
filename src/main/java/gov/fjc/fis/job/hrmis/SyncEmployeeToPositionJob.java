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

import static gov.fjc.fis.app.BigDecimalUtils.equalsIgnoreScale;

/**
 * Quartz job that synchronizes HRMIS Employee records with FIS Position records.
 * <p>
 * For each {@link Employee}, the job locates the corresponding {@link Position}
 * by position number. If no matching position exists, a new position is created
 * from the employee data. If a matching position exists, the configured fields
 * are compared and the position is updated when differences are found.
 * </p>
 *
 * <p>
 * Each created position and field-level change is recorded in a
 * {@link PositionAudit}. All position changes and their corresponding audit
 * records are persisted in a single save operation. Employees without a
 * position number are skipped.
 * </p>
 *
 * <p>
 * Standard hours and annual rate are not updated for positions with an
 * intermittent work schedule. Decimal values are compared without regard
 * to scale to prevent audit records and updates caused only by differences
 * in decimal representation.
 * </p>
 *
 * <p>
 * Concurrent execution is disabled because the job maintains a common
 * timestamp for the audit records produced during a single execution.
 * </p>
 *
 * @author Doug Mitchell
 * @see Employee
 * @see Position
 * @see PositionAudit
 */
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

    /**
     * Defines the mapping between an Employee field and its corresponding
     * Position field.
     *
     * @param fieldName      the display name used to identify the field in audit records
     * @param employeeGetter the function that retrieves the value from an employee
     * @param positionGetter the function that retrieves the current value from a position
     * @param positionSetter the function that assigns the employee value to a position
     * @param <V>            the type of value mapped between the entities
     */
    private record FieldMapping<V>(
            String fieldName,
            Function<Employee, V> employeeGetter,
            Function<Position, V> positionGetter,
            BiConsumer<Position, V> positionSetter) {
    }

    /**
     * Defines the Employee-to-Position field mappings used during synchronization.
     * <p>
     * Each mapping identifies the employee field to read, the corresponding
     * position field to compare and update, and the display name used when
     * creating {@code PositionAudit} records. The mappings drive the comparison
     * and update process, allowing employee data imported from HRMIS to be
     * synchronized with Position records using a common implementation.
     * </p>
     *
     * <p>
     * Mappings for standard hours and annual rate are subject to the special
     * processing rules for intermittent positions.
     * </p>
     */
    private static final List<FieldMapping<?>> FIELD_MAPPINGS = List.of(
            new FieldMapping<>("Name", Employee::getName,
                    Position::getName, Position::setName),
            new FieldMapping<>("emplid", Employee::getEmplid,
                    Position::getEmplid, Position::setEmplid),
            new FieldMapping<>("Job Title", Employee::getJobtitle,
                    Position::getJobtitle, Position::setJobtitle),
            new FieldMapping<>("Regular/Temp", Employee::getRegTemp,
                    Position::getRegTemp, Position::setRegTemp),
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

    /**
     * Synchronizes all imported Employee records with their corresponding
     * Position records.
     * <p>
     * Existing positions are updated when mapped employee values differ, while
     * missing positions are created from the employee data. Position changes and
     * their associated audit records are accumulated and saved together.
     * Employees that do not have a position number are skipped.
     * </p>
     *
     * @param context the Quartz context for the current job execution
     * @throws JobExecutionException if the employee or position data cannot be
     *                               loaded, synchronized, audited, or saved
     */
    @Authenticated
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("SyncEmployeeToPosition has started");

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

    /**
     * Loads all Employee records imported from the HRMIS feed.
     *
     * @return the imported employee records
     */
    private List<Employee> fetchAllEmployees() {
        return dataManager.load(Employee.class)
                .all()
                .list();
    }

    /**
     * Loads all Position records and indexes them by position number.
     * <p>
     * If multiple positions have the same position number, the first position
     * encountered is retained and a warning is written to the application log.
     * </p>
     *
     * @return the positions indexed by position number
     */
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

    /**
     * Creates a Position from an imported Employee and adds an audit record
     * documenting the creation.
     *
     * @param employee     the employee whose data will populate the new position
     * @param positionNbr  the position number assigned to the new position
     * @param auditEntries the collection to which the creation audit is added
     * @return the newly created position
     */
    private Position createPositionFromEmployee(Employee employee, String positionNbr,
                                                List<PositionAudit> auditEntries) {
        Position position = dataManager.create(Position.class);
        position.setPositionNbr(positionNbr);
        FIELD_MAPPINGS.forEach(mapping -> setField(employee, position, mapping));
        auditEntries.add(buildCreationAudit(position));
        return position;
    }

    /**
     * Copies a mapped field value from an Employee to a Position.
     *
     * @param employee the employee that supplies the value
     * @param position the position that receives the value
     * @param mapping  the field mapping used to retrieve and assign the value
     * @param <V>      the type of the mapped value
     */
    private <V> void setField(Employee employee, Position position, FieldMapping<V> mapping) {
        mapping.positionSetter().accept(position, mapping.employeeGetter().apply(employee));
    }

    /**
     * Applies all configured employee-to-position field mappings and records
     * an audit entry for each changed field.
     *
     * @param employee     the employee containing the current HRMIS values
     * @param position     the position to compare and update
     * @param auditEntries the collection to which field-change audits are added
     * @return {@code true} if at least one position field was changed;
     * otherwise {@code false}
     */
    private boolean applyChanges(Employee employee, Position position, List<PositionAudit> auditEntries) {
        boolean changed = false;
        for (FieldMapping<?> mapping : FIELD_MAPPINGS) {
            changed |= applyIfChanged(employee, position, mapping, auditEntries);
        }
        return changed;
    }

    /**
     * Updates a mapped Position field when its value differs from the corresponding
     * Employee field and adds an audit record describing the change.
     * <p>
     * Standard hours and annual rate are not changed when the position has an
     * intermittent work schedule.
     * </p>
     *
     * @param employee     the employee that supplies the new value
     * @param position     the position containing the current value
     * @param mapping      the mapping for the field being compared
     * @param auditEntries the collection to which a field-change audit is added
     * @param <V>          the type of the mapped value
     * @return {@code true} if the position field was changed; otherwise
     * {@code false}
     */
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

    /**
     * Determines whether two field values are equivalent.
     * <p>
     * {@link BigDecimal} values are compared numerically without regard to scale.
     * All other values are compared using {@link Objects#equals(Object, Object)}.
     * </p>
     *
     * @param oldValue the current position value
     * @param newValue the imported employee value
     * @return {@code true} if the values are equivalent; otherwise {@code false}
     */
    private boolean valuesEqual(Object oldValue, Object newValue) {
        // the scale is 2 in Employee, 0 in Position
        if (oldValue instanceof BigDecimal oldBd && newValue instanceof BigDecimal newBd) {
            return equalsIgnoreScale(oldBd, newBd);
        }
        return Objects.equals(oldValue, newValue);
    }

    /**
     * Creates an audit record documenting that a Position was created from an
     * imported HRMIS employee record.
     *
     * @param position the newly created position
     * @return the unsaved audit record for the position creation
     */
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

    /**
     * Creates an audit record documenting a change to a Position field.
     *
     * @param position  the position that was changed
     * @param fieldName the display name of the changed field
     * @param oldValue  the field value before the change, or {@code null}
     * @param newValue  the field value after the change, or {@code null}
     * @return the unsaved audit record for the field change
     */
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
