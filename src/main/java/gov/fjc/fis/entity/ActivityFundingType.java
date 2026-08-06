package gov.fjc.fis.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


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