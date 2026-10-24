package gov.fjc.fis.entity.dto;

import gov.fjc.fis.entity.ObjectCategory;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static gov.fjc.fis.app.BigDecimalUtils.sumNullSafe;
import static gov.fjc.fis.app.BigDecimalUtils.anyNonZero;

@JmixEntity(name = "fis_ObjectCategoryDto")
public class ObjectCategoryDto {
    @JmixGeneratedValue
    @JmixId
    private Integer id;

    private ObjectCategory category;

    private String majorObjectClass;

    private String title;

    private String titleAndCode;

    @Composition
    private List<ObjectClassDto> objectClasses;

    @Composition
    private List<DivisionDto> oneYearDivisions;

    @Composition
    private List<DivisionDto> twoYearDivisions;

    private BigDecimal totalOneYearAllocations = BigDecimal.ZERO;

    private BigDecimal totalTwoYearAllocations = BigDecimal.ZERO;

    private BigDecimal totalAllocations = BigDecimal.ZERO;

    private BigDecimal totalOneYearProjections = BigDecimal.ZERO;

    private BigDecimal totalTwoYearProjections = BigDecimal.ZERO;

    private BigDecimal totalProjections = BigDecimal.ZERO;

    private BigDecimal totalOneYearObligations = BigDecimal.ZERO;

    private BigDecimal totalTwoYearObligations = BigDecimal.ZERO;

    private BigDecimal totalObligations = BigDecimal.ZERO;

    private BigDecimal totalOneYearReimbursements = BigDecimal.ZERO;

    private BigDecimal totalTwoYearReimbursements = BigDecimal.ZERO;

    private BigDecimal totalReimbursements = BigDecimal.ZERO;

    private BigDecimal totalOneYearBalance = BigDecimal.ZERO;

    private BigDecimal totalTwoYearBalance = BigDecimal.ZERO;

    private BigDecimal totalBalance = BigDecimal.ZERO;

    private BigDecimal totalSpending = BigDecimal.ZERO;

    private BigDecimal totalOneYearSpending = BigDecimal.ZERO;

    private BigDecimal totalTwoYearSpending = BigDecimal.ZERO;

    // used by yellow book / budget request
    private BigDecimal oneYearProjected = BigDecimal.ZERO;

    private BigDecimal oneYearReimbursed = BigDecimal.ZERO;

    private BigDecimal oneYearObligated = BigDecimal.ZERO;

    private BigDecimal oneYearDisbursed = BigDecimal.ZERO;

    private BigDecimal priorYearProjected = BigDecimal.ZERO;

    private BigDecimal priorYearReimbursed = BigDecimal.ZERO;

    private BigDecimal priorYearObligated = BigDecimal.ZERO;

    private BigDecimal priorYearDisbursed = BigDecimal.ZERO;

    private BigDecimal twoYearProjected = BigDecimal.ZERO;

    private BigDecimal twoYearReimbursed = BigDecimal.ZERO;

    private BigDecimal twoYearObligated = BigDecimal.ZERO;

    private BigDecimal twoYearDisbursed = BigDecimal.ZERO;

    private BigDecimal discretionaryProjected = BigDecimal.ZERO;
    private BigDecimal discretionaryReimbursed = BigDecimal.ZERO;
    private BigDecimal discretionaryObligated = BigDecimal.ZERO;
    private BigDecimal discretionaryDisbursed = BigDecimal.ZERO;
    private BigDecimal mandatoryAllocated = BigDecimal.ZERO;
    private BigDecimal mandatoryProjected = BigDecimal.ZERO;
    private BigDecimal mandatoryReimbursed = BigDecimal.ZERO;
    private BigDecimal mandatoryObligated = BigDecimal.ZERO;
    private BigDecimal mandatoryDisbursed = BigDecimal.ZERO;

    public void configureCategoryDto(ObjectCategory category) {
        if (category != null) {
            this.majorObjectClass = category.getMajorObjectClass();
            this.title = category.getTitle();
            this.titleAndCode = String.format("%s (%s)", title, majorObjectClass);
        }
    }

    public void setOneYearProjected(BigDecimal oneYearProjected) {
        this.oneYearProjected = oneYearProjected;
    }

    public void setOneYearReimbursed(BigDecimal oneYearReimbursed) {
        this.oneYearReimbursed = oneYearReimbursed;
    }

    public void setOneYearObligated(BigDecimal oneYearObligated) {
        this.oneYearObligated = oneYearObligated;
    }

    public void setOneYearDisbursed(BigDecimal oneYearDisbursed) {
        this.oneYearDisbursed = oneYearDisbursed;
    }

    public void setPriorYearProjected(BigDecimal priorYearProjected) {
        this.priorYearProjected = priorYearProjected;
    }

    public void setPriorYearReimbursed(BigDecimal priorYearReimbursed) {
        this.priorYearReimbursed = priorYearReimbursed;
    }

    public void setPriorYearObligated(BigDecimal priorYearObligated) {
        this.priorYearObligated = priorYearObligated;
    }

    public void setPriorYearDisbursed(BigDecimal priorYearDisbursed) {
        this.priorYearDisbursed = priorYearDisbursed;
    }

    public void setTwoYearProjected(BigDecimal twoYearProjected) {
        this.twoYearProjected = twoYearProjected;
    }

    public void setTwoYearReimbursed(BigDecimal twoYearReimbursed) {
        this.twoYearReimbursed = twoYearReimbursed;
    }

    public void setTwoYearObligated(BigDecimal twoYearObligated) {
        this.twoYearObligated = twoYearObligated;
    }

    public void setTwoYearDisbursed(BigDecimal twoYearDisbursed) {
        this.twoYearDisbursed = twoYearDisbursed;
    }

    private Boolean showOnReport = Boolean.FALSE;

    public void setOneYearDivisions(List<DivisionDto> oneYearDivisions) {
        this.oneYearDivisions = oneYearDivisions;
    }

    public BigDecimal getTotalTwoYearSpending() {
        return totalTwoYearSpending;
    }

    public void setTotalTwoYearSpending(BigDecimal totalTwoYearSpending) {
        this.totalTwoYearSpending = totalTwoYearSpending;
    }

    public BigDecimal getTotalOneYearSpending() {
        return totalOneYearSpending;
    }

    public void setTotalOneYearSpending(BigDecimal totalOneYearSpending) {
        this.totalOneYearSpending = totalOneYearSpending;
    }

    public BigDecimal getTotalSpending() {
        return totalSpending;
    }

    public void setTotalSpending(BigDecimal totalSpending) {
        this.totalSpending = totalSpending;
    }

    public BigDecimal getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(BigDecimal totalBalance) {
        this.totalBalance = totalBalance;
    }

    public BigDecimal getTotalReimbursements() {
        return totalReimbursements;
    }

    public void setTotalReimbursements(BigDecimal totalReimbursements) {
        this.totalReimbursements = totalReimbursements;
    }

    public BigDecimal getTotalObligations() {
        return totalObligations;
    }

    public void setTotalObligations(BigDecimal totalObligations) {
        this.totalObligations = totalObligations;
    }

    public BigDecimal getTotalProjections() {
        return totalProjections;
    }

    public void setTotalProjections(BigDecimal totalProjections) {
        this.totalProjections = totalProjections;
    }

    public BigDecimal getTotalAllocations() {
        return totalAllocations;
    }

    public void setTotalAllocations(BigDecimal totalAllocations) {
        this.totalAllocations = totalAllocations;
    }

    public BigDecimal getTotalTwoYearBalance() {
        return totalTwoYearBalance;
    }

    public void setTotalTwoYearBalance(BigDecimal totalTwoYearBalance) {
        this.totalTwoYearBalance = totalTwoYearBalance;
    }

    public BigDecimal getTotalOneYearBalance() {
        return totalOneYearBalance;
    }

    public void setTotalOneYearBalance(BigDecimal totalOneYearBalance) {
        this.totalOneYearBalance = totalOneYearBalance;
    }

    public String getTitleAndCode() {
        return Objects.requireNonNullElseGet(titleAndCode, () -> String.format("%s (%s)", title, majorObjectClass));
    }

    public void setTitleAndCode(String titleAndCode) {
        this.titleAndCode = titleAndCode;
    }

    public ObjectCategory getCategory() {
        return category;
    }

    public void setCategory(ObjectCategory category) {
        this.category = category;
        this.id = category.getId();
        this.majorObjectClass = category.getMajorObjectClass();
        this.title = category.getTitle();
        this.titleAndCode = category.getTitle().concat(" (").concat(category.getMajorObjectClass().concat("00)"));
    }

    public List<ObjectClassDto> getObjectClasses() {
        return objectClasses;
    }

    public void setObjectClasses(List<ObjectClassDto> objectClasses) {
        this.objectClasses = objectClasses;
    }

    public void addObjectClasses(ObjectClassDto objectClassDto) {
        if (this.objectClasses == null) {
            this.objectClasses = new ArrayList<>();
        }
        this.objectClasses.add(objectClassDto);
    }

    public Boolean getShowOnReport() {
        return showOnReport ||
                anyNonZero(priorYearProjected, priorYearReimbursed, priorYearObligated, priorYearDisbursed,
                        oneYearProjected, oneYearReimbursed, oneYearObligated, oneYearDisbursed,
                        twoYearProjected, twoYearReimbursed, twoYearObligated, twoYearDisbursed,
                        totalOneYearAllocations, totalTwoYearAllocations,
                        totalOneYearObligations, totalTwoYearObligations,
                        totalOneYearProjections, totalTwoYearProjections,
                        totalOneYearReimbursements, totalTwoYearReimbursements, mandatoryAllocated,
                        mandatoryDisbursed, mandatoryObligated, mandatoryProjected, mandatoryReimbursed);
    }

    public void setShowOnReport(Boolean showOnReport) {
        this.showOnReport = showOnReport;
    }

    public BigDecimal getTotalTwoYearReimbursements() {
        return totalTwoYearReimbursements;
    }

    public void setTotalTwoYearReimbursements(BigDecimal totalTwoYearReimbursements) {
        this.totalTwoYearReimbursements = totalTwoYearReimbursements;
    }

    public BigDecimal getTotalOneYearReimbursements() {
        return totalOneYearReimbursements;
    }

    public void setTotalOneYearReimbursements(BigDecimal totalOneYearReimbursements) {
        this.totalOneYearReimbursements = totalOneYearReimbursements;
    }

    public BigDecimal getTotalTwoYearObligations() {
        return totalTwoYearObligations;
    }

    public void setTotalTwoYearObligations(BigDecimal totalTwoYearObligations) {
        this.totalTwoYearObligations = totalTwoYearObligations;
    }

    public BigDecimal getTotalOneYearObligations() {
        return totalOneYearObligations;
    }

    public void setTotalOneYearObligations(BigDecimal totalOneYearObligations) {
        this.totalOneYearObligations = totalOneYearObligations;
    }

    public BigDecimal getTotalTwoYearAllocations() {
        return totalTwoYearAllocations;
    }

    public void setTotalTwoYearAllocations(BigDecimal totalTwoYearAllocations) {
        this.totalTwoYearAllocations = totalTwoYearAllocations;
    }

    public BigDecimal getTotalOneYearAllocations() {
        return totalOneYearAllocations;
    }

    public void setTotalOneYearAllocations(BigDecimal totalOneYearAllocations) {
        this.totalOneYearAllocations = totalOneYearAllocations;
    }

    public BigDecimal getTotalTwoYearProjections() {
        return totalTwoYearProjections;
    }

    public void setTotalTwoYearProjections(BigDecimal totalTwoYearProjections) {
        this.totalTwoYearProjections = totalTwoYearProjections;
    }

    public BigDecimal getTotalOneYearProjections() {
        return totalOneYearProjections;
    }

    public void setTotalOneYearProjections(BigDecimal totalOneYearProjections) {
        this.totalOneYearProjections = totalOneYearProjections;
    }

    public List<DivisionDto> getTwoYearDivisions() {
        return twoYearDivisions;
    }

    public void setTwoYearDivisions(List<DivisionDto> twoYearDivisions) {
        this.twoYearDivisions = twoYearDivisions;
    }

    public List<DivisionDto> getOneYearDivisions() {
        return oneYearDivisions;
    }

    public void addOneYearDivision(DivisionDto division) {
        if (oneYearDivisions == null) {
            oneYearDivisions = new ArrayList<>();
        }
        oneYearDivisions.add(division);
    }

    public void addTwoYearDivision(DivisionDto division) {
        if (twoYearDivisions == null) {
            twoYearDivisions = new ArrayList<>();
        }
        twoYearDivisions.add(division);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMajorObjectClass() {
        return majorObjectClass;
    }

    public void setMajorObjectClass(String majorObjectClass) {
        this.majorObjectClass = majorObjectClass;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @InstanceName
    @DependsOnProperties({"title", "majorObjectClass"})
    public String getInstanceName() {
        return String.format("%s (%s)", title, majorObjectClass);
    }

    @DependsOnProperties({"oneYearDivisions"})
    public BigDecimal getOneYearAllocations(String divCode) {
        DivisionDto dto = getDivisionDto(oneYearDivisions, divCode);
        return dto == null ? BigDecimal.ZERO : dto.getOneYearAllocations();
    }

    @DependsOnProperties({"oneYearDivisions"})
    public BigDecimal getOneYearProjections(String divCode) {
        DivisionDto dto = getDivisionDto(oneYearDivisions, divCode);
        return dto == null ? BigDecimal.ZERO : dto.getOneYearProjections();
    }

    @DependsOnProperties({"oneYearDivisions"})
    public BigDecimal getOneYearObligations(String divCode) {
        DivisionDto dto = getDivisionDto(oneYearDivisions, divCode);
        return dto == null ? BigDecimal.ZERO : dto.getOneYearObligations();
    }

    @DependsOnProperties({"oneYearDivisions"})
    public BigDecimal getOneYearReimbursements(String divCode) {
        DivisionDto dto = getDivisionDto(oneYearDivisions, divCode);
        return dto == null ? BigDecimal.ZERO : dto.getOneYearReimbursements();
    }

    @DependsOnProperties({"twoYearDivisions"})
    public BigDecimal getTwoYearAllocations(String divCode) {
        DivisionDto dto = getDivisionDto(twoYearDivisions, divCode);
        return dto == null ? BigDecimal.ZERO : dto.getTwoYearAllocations();
    }

    @DependsOnProperties({"twoYearDivisions"})
    public BigDecimal getTwoYearObligations(String divCode) {
        DivisionDto dto = getDivisionDto(twoYearDivisions, divCode);
        return dto == null ? BigDecimal.ZERO : dto.getTwoYearObligations();
    }

    @DependsOnProperties({"twoYearDivisions"})
    public BigDecimal getTwoYearProjections(String divCode) {
        DivisionDto dto = getDivisionDto(twoYearDivisions, divCode);
        return dto == null ? BigDecimal.ZERO : dto.getTwoYearProjections();
    }

    @DependsOnProperties({"twoYearDivisions"})
    public BigDecimal getTwoYearReimbursements(String divCode) {
        DivisionDto dto = getDivisionDto(twoYearDivisions, divCode);
        return dto == null ? BigDecimal.ZERO : dto.getTwoYearReimbursements();
    }

    private DivisionDto getDivisionDto(List<DivisionDto> divisions, String divCode) {
        return divisions.stream()
                .filter(div -> divCode.equals(div.getDivisionCode()))
                .findAny()
                .orElse(null);
    }

    @DependsOnProperties({"totalOneYearAllocations"})
    public void addOneYearAllocation(BigDecimal amount) {
        totalOneYearAllocations = sumNullSafe(totalOneYearAllocations, amount);
    }

    @DependsOnProperties({"totalOneYearProjections"})
    public void addOneYearProjection(BigDecimal amount) {
        totalOneYearProjections = sumNullSafe(totalOneYearProjections, amount);
    }

    @DependsOnProperties({"totalOneYearObligations"})
    public void addOneYearObligation(BigDecimal amount) {
        totalOneYearObligations = sumNullSafe(totalOneYearObligations, amount);
    }

    public void addOneYearReimbursement(BigDecimal amount) {
        totalOneYearReimbursements = sumNullSafe(totalOneYearReimbursements, amount);
    }

    @DependsOnProperties({"totalTwoYearAllocations"})
    public void addTwoYearAllocation(BigDecimal amount) {
        totalTwoYearAllocations = sumNullSafe(totalTwoYearAllocations, amount);
    }

    public void addTwoYearProjection(BigDecimal amount) {
        totalTwoYearProjections = sumNullSafe(totalTwoYearProjections, amount);
    }

    public void addTwoYearObligation(BigDecimal amount) {
        totalTwoYearObligations = sumNullSafe(totalTwoYearObligations, amount);
    }

    public void addTwoYearReimbursement(BigDecimal amount) {
        totalTwoYearReimbursements = sumNullSafe(totalTwoYearReimbursements, amount);
    }

    public BigDecimal getOneYearProjected() {
        return oneYearProjected;
    }

    public void addCurrentYearProjected(BigDecimal amount) {
        this.oneYearProjected = sumNullSafe(this.oneYearProjected, amount);
    }

    public BigDecimal getOneYearReimbursed() {
        return oneYearReimbursed;
    }

    public void addCurrentYearReimbursed(BigDecimal amoount) {
        this.oneYearReimbursed = sumNullSafe(this.oneYearReimbursed, amoount);
    }

    public BigDecimal getOneYearObligated() {
        return oneYearObligated;
    }

    public void addCurrentYearObligated(BigDecimal amount) {
        this.oneYearObligated = sumNullSafe(this.oneYearObligated, amount);
    }

    public BigDecimal getOneYearDisbursed() {
        return oneYearDisbursed;
    }

    public void addCurrentYearDisbursed(BigDecimal amount) {
        this.oneYearDisbursed = sumNullSafe(this.oneYearDisbursed, amount);
    }

    public BigDecimal getPriorYearProjected() {
        return priorYearProjected;
    }

    public void addPriorYearProjected(BigDecimal amount) {
        this.priorYearProjected = sumNullSafe(this.priorYearProjected, amount);
    }

    public BigDecimal getPriorYearReimbursed() {
        return priorYearReimbursed;
    }

    public void addPriorYearReimbursed(BigDecimal amount) {
        this.priorYearReimbursed = sumNullSafe(this.priorYearReimbursed, amount);
    }

    public BigDecimal getPriorYearObligated() {
        return priorYearObligated;
    }

    public void addPriorYearObligated(BigDecimal amount) {
        this.priorYearObligated = sumNullSafe(this.priorYearObligated, amount);
    }

    public BigDecimal getPriorYearDisbursed() {
        return priorYearDisbursed;
    }

    public void addPriorYearDisbursed(BigDecimal amount) {
        this.priorYearDisbursed = sumNullSafe(this.priorYearDisbursed, amount);
    }

    public BigDecimal getTwoYearProjected() {
        return twoYearProjected;
    }

    public void addCurrentTwoYearProjected(BigDecimal amount) {
        this.twoYearProjected = sumNullSafe(this.twoYearProjected, amount);
    }

    public BigDecimal getTwoYearReimbursed() {
        return twoYearReimbursed;
    }

    public void addCurrentTwoYearReimbursed(BigDecimal nextYearReimbursed) {
        this.twoYearReimbursed = sumNullSafe(this.twoYearReimbursed, nextYearReimbursed);
    }

    public BigDecimal getTwoYearObligated() {
        return twoYearObligated;
    }

    public void addCurrentTwoYearObligated(BigDecimal amount) {
        this.twoYearObligated = sumNullSafe(this.twoYearObligated, amount);
    }

    public BigDecimal getTwoYearDisbursed() {
        return twoYearDisbursed;
    }

    public void addCurrentTwoYearDisbursed(BigDecimal amount) {
        this.twoYearDisbursed = sumNullSafe(this.twoYearDisbursed, amount);
    }

    public void calculateTotals() {
        setTotalAllocations(totalOneYearAllocations.add(totalTwoYearAllocations));
        setTotalProjections(totalOneYearProjections.add(totalTwoYearProjections));
        setTotalReimbursements(totalOneYearReimbursements.add(totalTwoYearReimbursements));
        setTotalObligations(totalOneYearObligations.add(totalTwoYearObligations));

        setTotalOneYearBalance(totalOneYearAllocations.subtract(totalOneYearObligations).subtract(totalOneYearProjections).add(totalOneYearReimbursements));
        setTotalTwoYearBalance(totalTwoYearAllocations.subtract(totalTwoYearObligations).subtract(totalTwoYearProjections).add(totalTwoYearReimbursements));
        setTotalBalance(totalOneYearBalance.add(totalTwoYearBalance));

        setTotalOneYearSpending(totalOneYearObligations.add(totalOneYearProjections));
        setTotalTwoYearSpending(totalTwoYearObligations.add(totalTwoYearProjections));
        setTotalSpending(totalOneYearSpending.add(totalTwoYearSpending));
    }

    public void calcTotalOneYearBalance() {
        setTotalOneYearBalance(totalOneYearAllocations.subtract(totalOneYearObligations).subtract(totalOneYearProjections).add(totalOneYearReimbursements));
    }

    public void calcTotalTwoYearBalance() {
        setTotalTwoYearBalance(totalTwoYearAllocations.subtract(totalTwoYearObligations).subtract(totalTwoYearProjections).add(totalTwoYearReimbursements));
    }

    public void addDiscretionaryProjected(BigDecimal amount) {
        discretionaryProjected = sumNullSafe(discretionaryProjected, amount);
    }

    public void addDiscretionaryObligated(BigDecimal amount) {
        discretionaryObligated = sumNullSafe(discretionaryObligated, amount);
    }

    public void addDiscretionaryDisbursed(BigDecimal amount) {
        discretionaryDisbursed = sumNullSafe(discretionaryDisbursed, amount);
    }

    public void addDiscretionaryReimbursed(BigDecimal amount) {
        discretionaryReimbursed = sumNullSafe(discretionaryReimbursed, amount);
    }

    public void addMandatoryProjected(BigDecimal amount) {
        mandatoryProjected = sumNullSafe(mandatoryProjected, amount);
    }

    public void addMandatoryObligated(BigDecimal amount) {
        mandatoryObligated = sumNullSafe(mandatoryObligated, amount);
    }

    public void addMandatoryDisbursed(BigDecimal amount) {
        mandatoryDisbursed = sumNullSafe(mandatoryDisbursed, amount);
    }

    public void addMandatoryReimbursed(BigDecimal amount) {
        mandatoryReimbursed = sumNullSafe(mandatoryReimbursed, amount);
    }

    public BigDecimal getDiscretionaryProjected() {
        return discretionaryProjected;
    }

    public void setDiscretionaryProjected(BigDecimal discretionaryProjected) {
        this.discretionaryProjected = discretionaryProjected;
    }

    public BigDecimal getDiscretionaryReimbursed() {
        return discretionaryReimbursed;
    }

    public void setDiscretionaryReimbursed(BigDecimal discretionaryReimbursed) {
        this.discretionaryReimbursed = discretionaryReimbursed;
    }

    public BigDecimal getDiscretionaryObligated() {
        return discretionaryObligated;
    }

    public void setDiscretionaryObligated(BigDecimal discretionaryObligated) {
        this.discretionaryObligated = discretionaryObligated;
    }

    public BigDecimal getDiscretionaryDisbursed() {
        return discretionaryDisbursed;
    }

    public void setDiscretionaryDisbursed(BigDecimal discretionaryDisbursed) {
        this.discretionaryDisbursed = discretionaryDisbursed;
    }

    public BigDecimal getMandatoryAllocated() {
        return mandatoryAllocated;
    }

    public void setMandatoryAllocated(BigDecimal mandatoryAllocated) {
        this.mandatoryAllocated = mandatoryAllocated;
    }

    public BigDecimal getMandatoryProjected() {
        return mandatoryProjected;
    }

    public void setMandatoryProjected(BigDecimal mandatoryProjected) {
        this.mandatoryProjected = mandatoryProjected;
    }

    public BigDecimal getMandatoryReimbursed() {
        return mandatoryReimbursed;
    }

    public void setMandatoryReimbursed(BigDecimal mandatoryReimbursed) {
        this.mandatoryReimbursed = mandatoryReimbursed;
    }

    public BigDecimal getMandatoryObligated() {
        return mandatoryObligated;
    }

    public void setMandatoryObligated(BigDecimal mandatoryObligated) {
        this.mandatoryObligated = mandatoryObligated;
    }

    public BigDecimal getMandatoryDisbursed() {
        return mandatoryDisbursed;
    }

    public void setMandatoryDisbursed(BigDecimal mandatoryDisbursed) {
        this.mandatoryDisbursed = mandatoryDisbursed;
    }

    @JmixProperty
    public BigDecimal getMandatoryBalance() {
        return getMandatoryAllocated()
                .subtract(getMandatoryProjected())
                .subtract(getMandatoryObligated())
                .subtract(getMandatoryDisbursed())
                .add(getMandatoryReimbursed());
    }

    /**
     * isTwoYearCategory determines whether any two year amounts are populated for the CategoryDto
     *
     * @return true if the CategoryDto if any two year amount is non-zero
     */
    public Boolean isTwoYearCategory() {
        return (totalTwoYearAllocations.compareTo(BigDecimal.ZERO) != 0)
                || (totalTwoYearProjections.compareTo(BigDecimal.ZERO) != 0)
                || (totalTwoYearObligations.compareTo(BigDecimal.ZERO) != 0)
                || (totalTwoYearReimbursements.compareTo(BigDecimal.ZERO) != 0);
    }

}