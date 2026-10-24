package gov.fjc.fis.service;

import gov.fjc.fis.app.BigDecimalUtils;
import gov.fjc.fis.entity.dto.ActivityProjectionDto;
import gov.fjc.fis.entity.dto.ActivityReimbursementDto;
import gov.fjc.fis.entity.dto.ObjectCategoryDto;
import gov.fjc.fis.entity.dto.ObligationDto;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * used by Budget Request report. This should be static method, not a component.
 */
@Component("fis_CategoryAmountAggregator")
public class CategoryAmountAggregator {

    public void accumulateAmounts(
            ObjectCategoryDto dto,
            List<ObligationDto> ob,
            List<ActivityProjectionDto> proj,
            List<ActivityReimbursementDto> reimb) {
        // Obligations
        dto.addPriorYearObligated(BigDecimalUtils.sum(ob, ObligationDto::getPriorTwoYearObligated));
        dto.addPriorYearDisbursed(BigDecimalUtils.sum(ob, ObligationDto::getPriorTwoYearDisbursed));
        dto.addCurrentYearObligated(BigDecimalUtils.sum(ob, ObligationDto::getCurrentOneYearObligated));
        dto.addCurrentYearDisbursed(BigDecimalUtils.sum(ob, ObligationDto::getCurrentOneYearDisbursed));
        dto.addCurrentTwoYearObligated(BigDecimalUtils.sum(ob, ObligationDto::getCurrentTwoYearObligated));
        dto.addCurrentTwoYearDisbursed(BigDecimalUtils.sum(ob, ObligationDto::getCurrentTwoYearDisbursed));

        // Projections
        dto.addPriorYearProjected(BigDecimalUtils.sum(proj, ActivityProjectionDto::getPriorTwoYearAmount));
        dto.addCurrentYearProjected(BigDecimalUtils.sum(proj, ActivityProjectionDto::getCurrentOneYearAmount));
        dto.addCurrentTwoYearProjected(BigDecimalUtils.sum(proj, ActivityProjectionDto::getCurrentTwoYearAmount));

        // Reimbursements
        dto.addPriorYearReimbursed(BigDecimalUtils.sum(reimb, ActivityReimbursementDto::getPriorTwoYearAmount));
        dto.addCurrentYearReimbursed(BigDecimalUtils.sum(reimb, ActivityReimbursementDto::getCurrentOneYearAmount));
        dto.addCurrentTwoYearReimbursed(BigDecimalUtils.sum(reimb, ActivityReimbursementDto::getCurrentTwoYearAmount));
    }
}
