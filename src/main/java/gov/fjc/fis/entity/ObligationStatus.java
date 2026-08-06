package gov.fjc.fis.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum ObligationStatus implements EnumClass<Integer> {

    OBLIGATED(10),
    DISBURSED(20),
    OBLIGATED_OR_DISBURSED(30);

    private final Integer id;

    ObligationStatus(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    @Nullable
    public static ObligationStatus fromId(Integer id) {
        for (ObligationStatus at : ObligationStatus.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}