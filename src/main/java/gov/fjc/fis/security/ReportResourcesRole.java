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