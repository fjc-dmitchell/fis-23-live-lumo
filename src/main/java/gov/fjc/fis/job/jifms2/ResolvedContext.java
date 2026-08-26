package gov.fjc.fis.job.jifms;

import gov.fjc.fis.entity.*;

public record ResolvedContext(
        Document document,
        Fund twoYearFund,
        Division educationDivision,
        Fund fund,
        Division division,
        Activity activity,
        Activity projectionActivity,
        ObjectClass objectClass,
        ObjectClass projectionObjectClass,
        Obligation obligation
) {
    /**
     * Convenience constructor for the initialCtx
     *
     * @param document          Document being processed
     * @param twoYearFund       Two Year Fund entity
     * @param educationDivision Education division for this Appropriation
     */
    ResolvedContext(Document document, Fund twoYearFund, Division educationDivision) {
        this(document, twoYearFund, educationDivision,
                null, null, null, null, null, null, null);
    }

    // temporary constructor to keep existing code working while I possibly refactor obligation into this
    ResolvedContext(Document document,
                    Fund twoYearFund,
                    Division educationDivision,
                    Fund fund,
                    Division division,
                    Activity activity,
                    Activity projectionActivity,
                    ObjectClass objectClass,
                    ObjectClass projectionObjectClass) {
        this(document, twoYearFund, educationDivision, fund, division, activity, projectionActivity, objectClass, projectionObjectClass, null);

    }
}
