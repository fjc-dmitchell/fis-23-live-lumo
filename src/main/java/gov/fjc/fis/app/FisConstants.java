package gov.fjc.fis.app;


/**
 * Provides application-wide constants used by FIS.
 *
 * <p>The constants defined by this class identify commonly referenced funds,
 * divisions, budget organizations, and major object categories. They are
 * primarily intended for use by service classes, JPQL security policies, and
 * Quartz jobs that must reference these values without depending on business
 * services.
 *
 * <p>This class cannot be instantiated.
 *
 * @author Doug Mitchell
 */
public final class FisConstants {

    private FisConstants() {
    }

    // =========================================================================
    // Fund Codes
    // =========================================================================

    /**
     * Appropriation One-year Fund code
     */
    public static final String ONE_YEAR_FUND_CODE = "092800";
    /**
     * Appropriation Multi-year Fund code
     */
    public static final String TWO_YEAR_FUND_CODE = "09280M";
    /**
     * FJC Foundation Fund code
     */
    public static final String FOUNDATION_FUND_CODE = "812300";
    /**
     * Judiciary Information Technology Fund code
     */
    public static final String JITF_FUND_CODE = "51140X";

    // =========================================================================
    // Division Codes
    // =========================================================================

    /**
     * General Administration Division Code
     */
    public static final String ADMINISTRATION_DIVISION_CODE = "1";
    /**
     * Education (formerly Court Education) Division Code
     */
    public static final String EDUCATION_DIVISION_CODE = "2";
    /**
     * Editorial (formerly CPD, PubMed) Division Code
     */
    public static final String EDITORIAL_DIVISION_CODE = "3";
    /**
     * Research Division Code
     */
    public static final String RESEARCH_DIVISION_CODE = "4";
    /**
     * Technology (formerly OSID, P&amp;T) Division Code
     */
    public static final String TECHNOLOGY_DIVISION_CODE = "5";
    /**
     * Judicial Education Division Code (defunct)
     */
    public static final String JED_DIVISION_CODE = "6";
    /**
     * Media Production Office division Code (defunct)
     */
    public static final String MPO_DIVISION_CODE = "7";
    /**
     * Judiciary Information Technology Fund division Code
     */
    public static final String JITF_DIVISION_CODE = "8";
    /**
     * One Big Beautiful Bill Act™ division code
     */
    public static final String OBBBA_DIVISION_CODE = "9";

    // =========================================================================
    // Division Budget Orgs.
    // =========================================================================
    /**
     * General Administration Budget Org.
     */
    public static final String ADMINISTRATION_BUDGET_ORG = "JXXXXXF";
    /**
     * Education (formerly Court Education) Budget Org.
     */
    public static final String EDUCATION_BUDGET_ORG = "JXXXXXA";
    /**
     * Editorial (formerly CPD, PubMed) Budget Org.
     */
    public static final String EDITORIAL_BUDGET_ORG = "JXXXXXD";
    /**
     * Research Budget Org.
     */
    public static final String RESEARCH_BUDGET_ORG = "JXXXXXC";
    /**
     * Technology (formerly OSID, P&amp;T) Budget Org.
     */
    public static final String TECHNOLOGY_BUDGET_ORG = "JXXXXXB";
    /**
     * Judicial Education Budget Org (defunct)
     */
    public static final String JED_BUDGET_ORG = "JXXXXXE";
    /**
     * Media Production Office Budget Org (defunct)
     */
    public static final String MPO_BUDGET_ORG = "JXXXXXG";
    /**
     * Judiciary Information Technology Fund Budget Org.
     */
    public static final String JITF_BUDGET_ORG = "JXXXXXF";
    /**
     * One Big Beautiful Bill Act™ Budget Org.
     */
    public static final String OBBBA_BUDGET_ORG = "JXXMAPP";

    // =========================================================================
    // Major Object Category Codes
    // =========================================================================
    /**
     * Personnel Compensation Major Object Category Code
     */
    public static final String PERSONNEL_COMPENSATION_MOC = "11";
    /**
     * Personnel Benefits Major Object Category Code
     */
    public static final String PERSONNEL_BENEFITS_MOC = "12";
    /**
     * Benefits for Former Personnel Major Object Category Code
     */
    public static final String PERSONNEL_PAYOUTS_MOC = "13";
    /**
     * Travel and Transportation of Persons Major Object Category Code
     */
    public static final String TRAVEL_MOC = "21";
    /**
     * JPQL pattern matching major object category codes for personnel expenses.
     */
    public static final String MOC_PERSONNEL_GENERIC_CODE = "1%";
}
