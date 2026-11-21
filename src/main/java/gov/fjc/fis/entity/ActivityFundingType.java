package gov.fjc.fis.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;

/**
 * Identifies the appropriation funding source used to finance an activity.
 *
 * <p>Activities conducted during the current fiscal year may be funded from
 * different appropriation-year sources depending on budget availability and
 * funding decisions. Funding may originate from the prior fiscal year's
 * multi-year fund, the current fiscal year's one-year fund, or the current
 * fiscal year's multi-year fund.
 *
 * <p>This enumeration is used primarily by financial reports to classify
 * activity funding and support analysis of expenditures by funding source.
 *
 * <p>Each value is persisted using an integer identifier through the Jmix
 * {@link EnumClass} interface.
 *
 * @author Doug Mitchell
 */
public enum ActivityFundingType implements EnumClass<Integer> {

    PRIOR_TWO_YEAR_FUND(0),
    CURRENT_ONE_YEAR_FUND(1),
    CURRENT_TWO_YEAR_FUND(2);

    private final Integer id;

    ActivityFundingType(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    @Nullable
    public static ActivityFundingType fromId(Integer id) {
        for (ActivityFundingType at : ActivityFundingType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}