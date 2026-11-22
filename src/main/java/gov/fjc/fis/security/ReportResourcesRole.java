package gov.fjc.fis.security;

import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportExecution;
import io.jmix.reports.entity.ReportGroup;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reports.entity.wizard.ReportData;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;

/**
 * Provides access to Jmix reporting resources required to execute FIS reports.
 * <p>
 * This role serves as a reusable base role that is inherited by other
 * resource roles requiring report functionality. It grants access to the
 * Jmix Reports entities and metadata used to locate report definitions,
 * organize reports, access report templates, and view report execution
 * information.
 * </p>
 * <p>
 * The role is intended to support execution of predefined financial and
 * operational reports within FIS. It does not grant access to business
 * entities or determine which data a user may view. Those permissions are
 * provided by the resource and data roles that implement this interface.
 * </p>
 * <p>
 * By centralizing report-related permissions in a single role, report access
 * can be shared across multiple security roles without duplicating the
 * underlying Jmix Reports configuration.
 * </p>
 * @author Doug Mitchell
 */
@ResourceRole(name = "Entities used by reports", code = ReportResourcesRole.CODE, scope = "UI", description = "run canned financial reports")
public interface ReportResourcesRole {
    String CODE = "resources-reports";

    @EntityAttributePolicy(entityClass = Report.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = Report.class, actions = EntityPolicyAction.READ)
    void report();

    @EntityAttributePolicy(entityClass = ReportData.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = ReportData.class, actions = EntityPolicyAction.READ)
    void reportData();

    @EntityAttributePolicy(entityClass = ReportExecution.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    void reportExecution();

    @EntityAttributePolicy(entityClass = ReportGroup.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = ReportGroup.class, actions = EntityPolicyAction.READ)
    void reportGroup();

    @EntityAttributePolicy(entityClass = ReportTemplate.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = ReportTemplate.class, actions = EntityPolicyAction.READ)
    void reportTemplate();
}