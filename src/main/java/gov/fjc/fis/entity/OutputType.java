package gov.fjc.fis.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum OutputType implements EnumClass<Integer> {

    PDF(10),
    EXCEL(20);

    private final Integer id;

    OutputType(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    @Nullable
    public static OutputType fromId(Integer id) {
        for (OutputType at : OutputType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}