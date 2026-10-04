package in.kanchuk.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "order_items")
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id")
    private ProductListing listing;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "product_snapshot", columnDefinition = "TEXT")
    private String productSnapshot;

    @Column(name = "taxable_value", precision = 12, scale = 2)
    private java.math.BigDecimal taxableValue;

    @Column(name = "gst_rate", precision = 5, scale = 4)
    private java.math.BigDecimal gstRate;

    @Column(name = "tax_amount", precision = 12, scale = 2)
    private java.math.BigDecimal taxAmount;

    @Column(name = "cgst", precision = 12, scale = 2)
    private java.math.BigDecimal cgst;

    @Column(name = "sgst", precision = 12, scale = 2)
    private java.math.BigDecimal sgst;

    @Column(name = "igst", precision = 12, scale = 2)
    private java.math.BigDecimal igst;

    @Column(name = "is_inter_state", nullable = false)
    private boolean isInterState = false;
}
