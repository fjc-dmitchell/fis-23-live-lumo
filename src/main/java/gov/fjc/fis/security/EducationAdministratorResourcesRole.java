package gov.fjc.fis.security;

import gov.fjc.fis.entity.*;
import gov.fjc.fis.entity.dto.*;
//import gov.fjc.fis.entity.form.Ape;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "Education Admin resources", code = EducationAdministratorResourcesRole.CODE, scope = "UI", description = "menu, screen, report access")
public interface EducationAdministratorResourcesRole extends UiMinimalRole, ReportResourcesRole {
    String CODE = "resources-ed-admin";

    @MenuPolicy(menuIds = {"fis_Division.list", "fis_Activity.list", "fis_Obligation.list", "fis_Invoice.list", "fis_FundControlNotice.list", "fis_Branch.list", "fis_Group.list", "fis_ReportRouter#openEducationProgramsReport", "fis_DistanceLearning.list", "fis_Ape.list", "fis_ReportRouter#openEducationBranchReport"})
    @ViewPolicy(viewIds = {"fis_Division.list", "fis_Activity.list", "fis_Obligation.list", "fis_Invoice.list", "fis_FundControlNotice.list", "fis_Branch.list", "fis_Group.list", "fis_ReportRouter#openEducationProgramsReport", "fis_DistanceLearning.list", "fis_Ape.list", "fis_EducationProgramsReportView", "fis_EducationBranchReportView"})
    void screens();

    @EntityAttributePolicy(entityClass = Activity.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = Activity.class, actions = EntityPolicyAction.READ)
    void activity();

    @EntityAttributePolicy(entityClass = ActivityDto.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = ActivityDto.class, actions = EntityPolicyAction.ALL)
    void activityDto();

    @EntityAttributePolicy(entityClass = ActivityProjectionDto.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = ActivityProjectionDto.class, actions = EntityPolicyAction.ALL)
    void activityProjectionDto();

    @EntityAttributePolicy(entityClass = ActivityProjection.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = ActivityProjection.class, actions = EntityPolicyAction.READ)
    void activityProjection();

    @EntityAttributePolicy(entityClass = ActivityReimbursement.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = ActivityReimbursement.class, actions = EntityPolicyAction.READ)
    void activityReimbursement();

    @EntityAttributePolicy(entityClass = ActivityReimbursementDto.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = ActivityReimbursementDto.class, actions = EntityPolicyAction.ALL)
    void activityReimbursementDto();

//    @EntityAttributePolicy(entityClass = Ape.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
//    @EntityPolicy(entityClass = Ape.class, actions = EntityPolicyAction.READ)
//    void ape();

    @EntityAttributePolicy(entityClass = Appropriation.class, attributes = {"id", "status", "version", "createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate", "createdByString", "budgetFiscalYear"}, action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = Appropriation.class, actions = EntityPolicyAction.READ)
    void appropriation();

    @EntityAttributePolicy(entityClass = AppropriationDto.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = AppropriationDto.class, actions = EntityPolicyAction.ALL)
    void appropriationDto();

    @EntityAttributePolicy(entityClass = Division.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = Division.class, actions = EntityPolicyAction.READ)
    void division();

    @EntityAttributePolicy(entityClass = DivisionAllocation.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = DivisionAllocation.class, actions = EntityPolicyAction.READ)
    void divisionAllocation();

    @EntityAttributePolicy(entityClass = ObjectCategory.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = ObjectCategory.class, actions = EntityPolicyAction.READ)
    void category();

    @EntityAttributePolicy(entityClass = ObjectCategoryDto.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = ObjectCategoryDto.class, actions = EntityPolicyAction.ALL)
    void categoryDto();

    @EntityAttributePolicy(entityClass = ObjectClassDto.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = ObjectClassDto.class, actions = EntityPolicyAction.ALL)
    void objectClassDto();

    @EntityAttributePolicy(entityClass = ObjectClass.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = ObjectClass.class, actions = EntityPolicyAction.READ)
    void objectClass();

    @EntityAttributePolicy(entityClass = Obligation.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = Obligation.class, actions = EntityPolicyAction.READ)
    void obligation();

    @EntityAttributePolicy(entityClass = ObligationDto.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = ObligationDto.class, actions = EntityPolicyAction.ALL)
    void obligationDto();

    @EntityAttributePolicy(entityClass = Invoice.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = Invoice.class, actions = EntityPolicyAction.READ)
    void invoice();

    @EntityAttributePolicy(entityClass = GroupDto.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = GroupDto.class, actions = EntityPolicyAction.ALL)
    void groupDto();

    @EntityAttributePolicy(entityClass = Group.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = Group.class, actions = EntityPolicyAction.READ)
    void group();

    @EntityAttributePolicy(entityClass = FundControlNotice.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = FundControlNotice.class, actions = EntityPolicyAction.READ)
    void fundControlNotice();

    @EntityAttributePolicy(entityClass = Fund.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = Fund.class, actions = EntityPolicyAction.READ)
    void fund();

    @EntityAttributePolicy(entityClass = Branch.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = Branch.class, actions = EntityPolicyAction.READ)
    void branch();
}