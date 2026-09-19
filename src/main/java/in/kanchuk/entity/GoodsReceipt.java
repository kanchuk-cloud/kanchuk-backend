package in.kanchuk.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "goods_receipts")
public class GoodsReceipt extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "po_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @Column(name = "grn_number", nullable = false, unique = true, length = 50)
    private String grnNumber;

    @Column(nullable = false, length = 20)
    private String status = "pending";

    // Logistics
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private StockLocation location;

    @Column(name = "carrier_name", length = 100)
    private String carrierName;

    @Column(name = "vehicle_number", length = 50)
    private String vehicleNumber;

    @Column(name = "invoice_number", length = 100)
    private String invoiceNumber;

    @Column(name = "invoice_date")
    private LocalDate invoiceDate;

    // Personnel
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "received_by")
    private AdminUser receivedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspected_by")
    private AdminUser inspectedBy;

    @Column(name = "inspected_at")
    private OffsetDateTime inspectedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "received_at", nullable = false)
    private OffsetDateTime receivedAt = OffsetDateTime.now();

    // Vendor portal fields (added in V20)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by_vendor_user_id")
    private VendorUser submittedByVendorUser;

    @Column(name = "inventory_updated", nullable = false)
    private boolean inventoryUpdated = false;

    @Column(name = "rejection_notes", columnDefinition = "TEXT")
    private String rejectionNotes;
}
