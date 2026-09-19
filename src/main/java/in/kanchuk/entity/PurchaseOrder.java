package in.kanchuk.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder extends BaseEntity {

    @Column(name = "po_number", nullable = false, unique = true, length = 50)
    private String poNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private Seller seller;

    @Column(nullable = false, length = 30)
    private String status = "draft";

    // Financial
    @Column(precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "tax_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "shipping_charges", nullable = false, precision = 10, scale = 2)
    private BigDecimal shippingCharges = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "total_items")
    private Integer totalItems = 0;

    // Currency
    @Column(nullable = false, length = 3)
    private String currency = "INR";

    @Column(name = "exchange_rate", nullable = false, precision = 10, scale = 4)
    private BigDecimal exchangeRate = BigDecimal.ONE;

    // Vendor / shipping
    @Column(name = "payment_terms", length = 100)
    private String paymentTerms;

    @Column(name = "payment_status", length = 20)
    private String paymentStatus = "pending";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "shipping_address")
    private String shippingAddress;

    @Column(name = "vendor_invoice_number", length = 100)
    private String vendorInvoiceNumber;

    @Column(name = "vendor_invoice_date")
    private LocalDate vendorInvoiceDate;

    // Approval
    @Column(name = "approval_required", nullable = false)
    private boolean approvalRequired = false;

    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private AdminUser approvedBy;

    // Status timestamps
    @Column(name = "sent_at")
    private OffsetDateTime sentAt;

    @Column(name = "confirmed_at")
    private OffsetDateTime confirmedAt;

    @Column(name = "expected_at")
    private OffsetDateTime expectedAt;

    @Column(name = "received_at")
    private OffsetDateTime receivedAt;

    @Column(name = "cancelled_at")
    private OffsetDateTime cancelledAt;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    // Tracking
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private AdminUser createdBy;

    @Column(name = "is_recurring", nullable = false)
    private boolean isRecurring = false;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
