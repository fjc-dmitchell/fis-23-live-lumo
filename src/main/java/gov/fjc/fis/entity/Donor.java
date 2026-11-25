package gov.fjc.fis.entity;

import gov.fjc.fis.app.StringUtils;
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

import static gov.fjc.fis.app.AuditTextUtils.getCreatedModifiedString;
import static gov.fjc.fis.app.FisUtilities.*;

/**
 * Represents a donor that contributes funds to the FJC Foundation.
 *
 * <p>A donor may be an individual, corporation, nonprofit organization,
 * foundation, government entity, or other organization that provides
 * financial support to Foundation-sponsored programs, projects, events,
 * or initiatives.
 *
 * <p>This entity maintains donor contact information, including mailing
 * address, contact person, telephone numbers, email address, and other
 * notes relevant to the donor relationship. Donor records provide a
 * centralized source of information for managing contributions and
 * Foundation-related fundraising activities.
 *
 * <p>Audit information is maintained to track when donor records were
 * created or modified and by whom.
 *
 * @author Doug Mitchell
 */
@JmixEntity
@Table(name = "FIS_DONOR")
@Entity(name = "fis_Donor")
public class Donor {
    @Column(name = "ID", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @InstanceName
    @Column(name = "ORGANIZATION", nullable = false)
    @NotNull
    private String organization;

    @Column(name = "ADDRESS1")
    private String address1;

    @Column(name = "ADDRESS2")
    private String address2;

    @Column(name = "CITY")
    private String city;

    @Column(name = "STATE", length = 2)
    private String state;

    @Column(name = "ZIPCODE", length = 10)
    private String zipCode;

    @Column(name = "CONTACT")
    private String contact;

    @Column(name = "PHONE")
    private String phone;

    @Column(name = "FAX")
    private String fax;

    @Column(name = "EMAIL")
    private String email;

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

    @DependsOnProperties({"createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate"})
    @JmixProperty
    public String getCreatedByString() {
        return getCreatedModifiedString(createdBy, createdDate, lastModifiedBy, lastModifiedDate);
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = safeTrim(memo);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = safeTrim(email);
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = safeTrim(fax);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = safeTrim(phone);
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = safeTrim(contact);
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = safeTrim(zipCode);
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = safeTrim(StringUtils.toUpperCase(state));
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = safeTrim(city);
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = safeTrim(address2);
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = safeTrim(address1);
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = safeTrim(organization);
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
}