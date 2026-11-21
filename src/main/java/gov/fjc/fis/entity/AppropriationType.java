package gov.fjc.fis.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;

/**
 * Identifies the appropriation funding category used for financial
 * reporting and budget analysis.
 *
 * <p>This enumeration is used by financial reports to aggregate budget
 * authority, obligations, expenses, and other financial activity by
 * appropriation funding source. Reports may be generated using only the
 * appropriation's one-year fund, only the multi-year fund, a combined
 * view of both funds, or the special-purpose OBBBA appropriation adjustment.
 *
 * <p>The OBBBA adjustment represents funding provided under the
 * One Big Beautiful Bill Act (OBBBA), which temporarily supplements
 * the normal appropriation funding structure.
 *
 * <p>Each value is persisted using an integer identifier through the
 * Jmix {@link EnumClass} interface.
 *
 * @author Doug Mitchell
 */
public enum AppropriationType implements EnumClass<Integer> {

    ONE_YEAR_FUND(10),
    TWO_YEAR_FUND(20),
    COMBINED_YEAR_FUND(30),
    OBBBA_ADJUSTMENT(40);

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