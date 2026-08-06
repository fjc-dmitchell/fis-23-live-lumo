package gov.fjc.fis.entity;

import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.OffsetDateTime;

import static gov.fjc.fis.FisUtilities.getCreatedModifiedString;

@JmixEntity
@Table(name = "FIS_DOCUMENT_EXCEPTION", indexes = {
        @Index(name = "IDX_FIS_DOCUMENT_EXCEPTION_UNQ", columnList = "FUND_CODE, BBFY, BUDGET_ORG, BOC, DOCUMENT_TYPE, DOCUMENT_NUMBER", unique = true)
})
@Entity(name = "fis_DocumentException")
public class DocumentException {
    @Column(name = "ID", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "FUND_CODE", nullable = false, length = 6)
    @NotNull
    private String fundCode;

    @NotNull
    @Column(name = "BBFY", nullable = false, length = 4)
    private String bbfy;

    @Column(name = "BUDGET_ORG", nullable = false, length = 7)
    @NotNull
    private String budgetOrg;

    @Column(name = "BOC", nullable = false, length = 7)
    @NotNull
    private String budgetObjectClass;

    @Column(name = "DOCUMENT_TYPE", nullable = false, length = 5)
    @NotNull
    private String documentType;

    @Column(name = "DOCUMENT_NUMBER", nullable = false, length = 50)
    @NotNull
    private String documentNumber;

    @Column(name = "VERSION", nullable = false)
    @Version
    private Integer version;

    @CreatedBy
    @Column(name = "CREATED_BY")
    private String createdBy;

    @CreatedDate
    @Column(name = "CREATED_DATE")
    private OffsetDateTime createdDate;

    @LastModifiedBy
    @Column(name = "LAST_MODIFIED_BY")
    private String lastModifiedBy;

    @LastModifiedDate
    @Column(name = "LAST_MODIFIED_DATE")
    private OffsetDateTime lastModifiedDate;

    @DependsOnProperties({"createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate"})
    @JmixProperty
    public String getCreatedByString() {
        return getCreatedModifiedString(createdBy, createdDate, lastModifiedBy, lastModifiedDate);
    }

    public String getBudgetOrg() {
        return budgetOrg;
    }

    public void setBudgetOrg(String budgetOrg) {
        this.budgetOrg = budgetOrg;
    }

    public String getFundCode() {
        return fundCode;
    }

    public void setFundCode(String fundCode) {
        this.fundCode = fundCode;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getBudgetObjectClass() {
        return budgetObjectClass;
    }

    public void setBudgetObjectClass(String budgetObjectClass) {
        this.budgetObjectClass = budgetObjectClass;
    }

    public void setBbfy(String bbfy) {
        this.bbfy = bbfy;
    }

    public String getBbfy() {
        return bbfy;
    }

    public OffsetDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(OffsetDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public OffsetDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(OffsetDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @InstanceName
    @DependsOnProperties({"fundCode", "documentType", "bbfy", "budgetOrg", "budgetObjectClass", "documentNumber"})
    public String getInstanceName(MetadataTools metadataTools) {
        return String.format("%s-%s-%s-%s-%s-%s",
                metadataTools.format(fundCode),
                metadataTools.format(documentType),
                metadataTools.format(bbfy),
                metadataTools.format(budgetOrg),
                metadataTools.format(budgetObjectClass),
                metadataTools.format(documentNumber));
    }
}