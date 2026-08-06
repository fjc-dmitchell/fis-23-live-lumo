package gov.fjc.fis.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum AppropriationType implements EnumClass<Integer> {

    ONE_YEAR_FUND(10),
    TWO_YEAR_FUND(20),
    COMBINED_YEAR_FUND(30),
    OBBBA_APPROPRIATION(40);

    private final Integer id;

    AppropriationType(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    @Nullable
    public static AppropriationType fromId(Integer id) {
        for (AppropriationType at : AppropriationType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}