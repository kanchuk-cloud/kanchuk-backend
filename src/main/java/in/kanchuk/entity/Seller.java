package in.kanchuk.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "sellers")
public class Seller extends SoftDeleteEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(unique = true, length = 255)
    private String email;

    @Column(length = 15)
    private String phone;

    @Column(length = 15)
    private String gstin;

    @Column(name = "state_code", length = 2)
    private String stateCode;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    // Vendor / procurement fields (added in V6)
    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "bank_account", length = 50)
    private String bankAccount;

    @Column(name = "bank_ifsc", length = 11)
    private String bankIfsc;

    @Column(name = "payment_terms", length = 100)
    private String paymentTerms;

    @Column(name = "lead_time_days")
    private Integer leadTimeDays;

    @Column(name = "is_preferred", nullable = false)
    private boolean isPreferred = false;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Vendor onboarding fields (added in V29)
    @Column(name = "status", length = 20, nullable = false)
    private String status = "pending";

    @Column(name = "legal_name", length = 200)
    private String legalName;

    @Column(name = "brand_name", length = 200)
    private String brandName;

    @Column(name = "business_type", length = 30)
    private String businessType;

    @Column(name = "pan_number", length = 10)
    private String panNumber;

    @Column(name = "is_msme", nullable = false)
    private boolean isMsme = false;

    @Column(name = "udyam_number", length = 20)
    private String udyamNumber;

    @Column(name = "mobile", length = 10)
    private String mobile;

    @Column(name = "address_line", length = 255)
    private String addressLine;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "pincode", length = 6)
    private String pincode;

    /** Stored raw; Lombok getter suppressed — use getBankAccountNumber() which returns masked value. */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Column(name = "bank_account_number", length = 50)
    private String bankAccountNumberRaw;

    @Column(name = "bank_account_holder_name", length = 200)
    private String bankAccountHolderName;

    @Column(name = "commission_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionPercent = BigDecimal.ZERO;

    @Column(name = "payment_cycle_days", nullable = false)
    private int paymentCycleDays = 30;

    @Column(name = "kyc_status", length = 20, nullable = false)
    private String kycStatus = "not_submitted";

    /** Returns masked account number (last 4 digits). */
    public String getBankAccountNumber() {
        if (bankAccountNumberRaw == null || bankAccountNumberRaw.length() <= 4) return bankAccountNumberRaw;
        return "****" + bankAccountNumberRaw.substring(bankAccountNumberRaw.length() - 4);
    }

    public void setBankAccountNumber(String raw) {
        this.bankAccountNumberRaw = raw;
    }
}
