package gov.fjc.fis.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum ProjectionType implements EnumClass<Integer> {

    MULTIPLE_BOC(1),
    SINGLE_GENERIC_BOC(20);

    private final Integer id;

    ProjectionType(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    @Nullable
    public static ProjectionType fromId(Integer id) {
        for (ProjectionType at : ProjectionType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}