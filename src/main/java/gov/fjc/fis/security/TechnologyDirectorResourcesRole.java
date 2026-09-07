package gov.fjc.fis.security;

import gov.fjc.fis.entity.*;
import gov.fjc.fis.entity.dto.*;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "ITO Director resources", code = TechnologyDirectorResourcesRole.CODE, scope = "UI", description = "entity and report access")
public interface TechnologyDirectorResourcesRole extends UiMinimalRole, ReportResourcesRole {
    String CODE = "resources-ito-director";

    @EntityAttributePolicy(entityClass = Activity.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = Activity.class, actions = EntityPolicyAction.READ)
    void activity();

    @EntityAttributePolicy(entityClass = Appropriation.class, attributes = {"id", "budgetFiscalYear"}, action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = Appropriation.class, actions = EntityPolicyAction.READ)
    void appropriation();

    @EntityAttributePolicy(entityClass = Branch.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = Branch.class, actions = EntityPolicyAction.READ)
    void branch();

    @EntityAttributePolicy(entityClass = Division.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = Division.class, actions = EntityPolicyAction.READ)
    void division();

    @EntityAttributePolicy(entityClass = Fund.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = Fund.class, actions = EntityPolicyAction.READ)
    void fund();

    @EntityAttributePolicy(entityClass = Obligation.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = Obligation.class, actions = EntityPolicyAction.READ)
    void obligation();

    @EntityAttributePolicy(entityClass = ObligationDto.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = ObligationDto.class, actions = EntityPolicyAction.ALL)
    void obligationDto();

    @MenuPolicy(menuIds = "fis_ReportRouter#openDivisionObligationsReport")
    @ViewPolicy(viewIds = "fis_DivisionObligationsReportView")
    void screens();

    @EntityPolicy(entityClass = ActivityDto.class, actions = EntityPolicyAction.ALL)
    void activityDto();

    @EntityPolicy(entityClass = ActivityProjectionDto.class, actions = EntityPolicyAction.ALL)
    void activityProjectionDto();

    @EntityPolicy(entityClass = ActivityReimbursementDto.class, actions = EntityPolicyAction.ALL)
    void activityReimbursementDto();

    @EntityPolicy(entityClass = AmountsDto.class, actions = EntityPolicyAction.ALL)
    void amountsDto();

    @EntityPolicy(entityClass = AppropriationDto.class, actions = EntityPolicyAction.ALL)
    void appropriationDto();

    @EntityPolicy(entityClass = BranchDto.class, actions = EntityPolicyAction.ALL)
    void branchDto();

    @EntityPolicy(entityClass = DivisionDto.class, actions = EntityPolicyAction.ALL)
    void divisionDto();

    @EntityPolicy(entityClass = GroupDto.class, actions = EntityPolicyAction.ALL)
    void groupDto();

    @EntityPolicy(entityClass = JitfTransferDto.class, actions = EntityPolicyAction.ALL)
    void jitfTransferDto();

    @EntityPolicy(entityClass = ObjectCategoryDto.class, actions = EntityPolicyAction.ALL)
    void objectCategoryDto();

    @EntityPolicy(entityClass = ObjectClassDto.class, actions = EntityPolicyAction.ALL)
    void objectClassDto();
}