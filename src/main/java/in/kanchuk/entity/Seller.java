package in.kanchuk.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
}
