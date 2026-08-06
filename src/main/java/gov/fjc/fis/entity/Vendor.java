package gov.fjc.fis.entity;

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

import static gov.fjc.fis.FisUtilities.*;

@JmixEntity
@Table(name = "FIS_VENDOR", uniqueConstraints = {
        @UniqueConstraint(name = "IDX_FIS_VENDOR_UNQ", columnNames = {"VENDOR_CODE", "ADDRESS_CODE"})
})
@Entity(name = "fis_Vendor")
public class Vendor {
    @Column(name = "ID", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @InstanceName
    @Column(name = "NAME", nullable = false)
    @NotNull
    private String name;
    @Column(name = "EIN", length = 10)
    private String ein;
    @NotNull
    @Column(name = "VENDOR_CODE", nullable = false, length = 10)
    private String vendorCode;
    @NotNull
    @Column(name = "ADDRESS_CODE", nullable = false, length = 15)
    private String addressCode;
    @Column(name = "ADDRESS1")
    private String address1;
    @Column(name = "ADDRESS2")
    private String address2;
    @Column(name = "CITY")
    private String city;
    @Column(name = "STATE", length = 2)
    private String state;
    @Column(name = "ZIP_CODE", length = 10)
    private String zipCode;
    @Column(name = "DUNS", length = 9)
    private String duns;
    @Column(name = "CAGE", length = 5)
    private String cage;
    @Column(name = "ACTIVE")
    private Boolean active;

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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @DependsOnProperties({"createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate"})
    @JmixProperty
    public String getCreatedByString() {
        return getCreatedModifiedString(createdBy, createdDate, lastModifiedBy, lastModifiedDate);
    }

    public String getCage() {
        return cage;
    }

    public void setCage(String cage) {
        this.cage = safeTrim(cage);
    }

    public String getDuns() {
        return duns;
    }

    public void setDuns(String duns) {
        this.duns = safeTrim(duns);
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
        this.state = safeToUpperCase(safeTrim(state));
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

    public String getVendorCode() {
        return vendorCode;
    }

    public void setVendorCode(String vendorCode) {
        this.vendorCode = safeTrim(vendorCode);
    }

    public String getAddressCode() {
        return addressCode;
    }

    public void setAddressCode(String addressCode) {
        this.addressCode = safeTrim(addressCode);
    }

    public String getEin() {
        return ein;
    }

    public void setEin(String ein) {
        this.ein = safeTrim(ein);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = safeTrim(name);
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