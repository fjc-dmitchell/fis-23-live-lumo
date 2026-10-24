package gov.fjc.fis.service;

import gov.fjc.fis.app.BigDecimalUtils;
import gov.fjc.fis.entity.dto.ActivityProjectionDto;
import gov.fjc.fis.entity.dto.ActivityReimbursementDto;
import gov.fjc.fis.entity.dto.ObjectCategoryDto;
import gov.fjc.fis.entity.dto.ObligationDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

/**
 * used by Budget Request report. Should be static method,not a component. Magic number is an issue.
 */
@Component("fis_CategoryFundTypeAggregator")
public class CategoryFundTypeAggregator {

    private static final String MANDATORY_DIV = "9";

    private final Predicate<ObligationDto> isDiscretionaryOb =
            o -> !MANDATORY_DIV.equals(o.getDivisionCode());
    private final Predicate<ObligationDto> isMandatoryOb =
            o -> MANDATORY_DIV.equals(o.getDivisionCode());

    private final Predicate<ActivityProjectionDto> isDiscretionaryProj =
            p -> !MANDATORY_DIV.equals(p.getDivisionCode());
    private final Predicate<ActivityProjectionDto> isMandatoryProj =
            p -> MANDATORY_DIV.equals(p.getDivisionCode());

    private final Predicate<ActivityReimbursementDto> isDiscretionaryReimb =
            r -> !MANDATORY_DIV.equals(r.getDivisionCode());
    private final Predicate<ActivityReimbursementDto> isMandatoryReimb =
            r -> MANDATORY_DIV.equals(r.getDivisionCode());

    public void accumulateFundTypes(
            ObjectCategoryDto dto,
            List<ObligationDto> ob,
            List<ActivityProjectionDto> proj,
            List<ActivityReimbursementDto> reimb) {
        // DISCRETIONARY
        dto.addDiscretionaryObligated(
                BigDecimalUtils.sumFiltered(ob, isDiscretionaryOb, ObligationDto::getCurrentOneYearObligated));
        dto.addDiscretionaryDisbursed(
                BigDecimalUtils.sumFiltered(ob, isDiscretionaryOb, ObligationDto::getCurrentOneYearDisbursed));
        dto.addDiscretionaryProjected(
                BigDecimalUtils.sumFiltered(proj, isDiscretionaryProj, ActivityProjectionDto::getCurrentOneYearAmount));
        dto.addDiscretionaryReimbursed(
                BigDecimalUtils.sumFiltered(reimb, isDiscretionaryReimb, ActivityReimbursementDto::getCurrentOneYearAmount));

        // MANDATORY
        dto.addMandatoryObligated(
                BigDecimalUtils.sumFiltered(ob, isMandatoryOb, ObligationDto::getCurrentOneYearObligated));
        dto.addMandatoryDisbursed(
                BigDecimalUtils.sumFiltered(ob, isMandatoryOb, ObligationDto::getCurrentOneYearDisbursed));
        dto.addMandatoryProjected(
                BigDecimalUtils.sumFiltered(proj, isMandatoryProj, ActivityProjectionDto::getCurrentOneYearAmount));
        dto.addMandatoryReimbursed(
                BigDecimalUtils.sumFiltered(reimb, isMandatoryReimb, ActivityReimbursementDto::getCurrentOneYearAmount));
    }
}