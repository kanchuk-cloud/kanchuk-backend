package in.kanchuk.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "dispatching")
public class Dispatching extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "packaging_id")
    private Packaging packaging;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fulfillment_id")
    private Fulfillment fulfillment;

    @Column(name = "dispatched_by", length = 100)
    private String dispatchedBy;

    @Column(length = 100)
    private String carrier;

    @Column(name = "awb_number", length = 100)
    private String awbNumber;

    @Column(name = "dispatch_type", nullable = false, length = 20)
    private String dispatchType = "courier_pickup";

    @Column(nullable = false, length = 20)
    private String status = "pending";

    @Column(name = "dispatched_at")
    private OffsetDateTime dispatchedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
