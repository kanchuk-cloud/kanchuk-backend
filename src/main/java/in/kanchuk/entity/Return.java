package in.kanchuk.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "returns")
public class Return extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    @Column(nullable = false, length = 30)
    private String status = "requested";

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "coins_refunded")
    private Integer coinsRefunded;

    @Column(name = "requested_at")
    private OffsetDateTime requestedAt;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    // Reverse pickup
    @Column(name = "awb_number", length = 100)
    private String awbNumber;

    @Column(name = "carrier", length = 100)
    private String carrier;

    @Column(name = "pickup_scheduled_at")
    private OffsetDateTime pickupScheduledAt;

    // Warehouse
    @Column(name = "received_at")
    private OffsetDateTime receivedAt;

    @Column(name = "inspection_notes", columnDefinition = "TEXT")
    private String inspectionNotes;

    // Rejection
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    // SLA
    @Column(name = "expected_return_by")
    private OffsetDateTime expectedReturnBy;
}
