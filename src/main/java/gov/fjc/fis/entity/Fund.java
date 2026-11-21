package gov.fjc.fis.entity;

import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.OffsetDateTime;

import static gov.fjc.fis.app.FisUtilities.*;

/**
 * Represents a funding source used to finance activities, programs,
 * and projects.
 *
 * <p>A fund identifies the source of financing associated with an activity
 * and is used for budgeting, planning, financial management, and reporting.
 * Funds may represent appropriated resources, reimbursable funding, or
 * other authorized sources of program support.</p>
 *
 * <p>Examples include the one-year portion of an appropriation, the
 * multi-year portion of an appropriation, the Federal Judicial Center
 * Foundation, and the Judiciary Information Technology Fund (JITF).
 * Each fund is uniquely identified by a fund code and title and may be
 * referenced by divisions, activities, and other financial records
 * throughout the system.</p>
 *
 * <p>The entity provides descriptive and audit information for use in the
 * Jmix user interface and supports optimistic locking and auditing through
 * standard Jmix persistence facilities.</p>
 *
 * @author Doug Mitchell
 */
@JmixEntity
@Table(name = "FIS_FUND", uniqueConstraints = {
        @UniqueConstraint(name = "IDX_FIS_FUND_UNQ_CODE", columnNames = {"FUND_CODE"}),
        @UniqueConstraint(name = "IDX_FIS_FUND_UNQ_TITLE", columnNames = {"TITLE"})
})
@Entity(name = "fis_Fund")
public class Fund {
    @Column(name = "ID", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Length(min = 6, max = 6)
    @Column(name = "FUND_CODE", nullable = false, length = 6)
    @NotNull
    private String fundCode;

    @Column(name = "TITLE", nullable = false)
    @NotNull
    private String title;

    @Column(name = "MEMO")
    @Lob
    private String memo;
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

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = safeTrim(memo);
    }

    @DependsOnProperties({"title", "fundCode"})
    @JmixProperty
    public String getTitleAndCode() {
        return String.format("%s (%s)", title, fundCode);
    }

    @DependsOnProperties({"createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate"})
    @JmixProperty
    public String getCreatedByString() {
        return getCreatedModifiedString(createdBy, createdDate, lastModifiedBy, lastModifiedDate);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = safeTrim(title);
    }

    public String getFundCode() {
        return fundCode;
    }

    public void setFundCode(String fundCode) {
        this.fundCode = safeToUpperCase(fundCode);
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
    @DependsOnProperties({"title", "fundCode"})
    public String getInstanceName(MetadataTools metadataTools) {
        return String.format("%s (%s)",
                metadataTools.format(title),
                metadataTools.format(fundCode));
    }
}