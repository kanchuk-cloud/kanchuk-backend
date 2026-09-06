package in.kanchuk.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 30)
    private String status = "pending";

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "delivery_charge", nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryCharge = BigDecimal.ZERO;

    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_method_id")
    private ShippingMethod shippingMethod;

    @JsonIgnore
    @Column(name = "delivery_address_snapshot", columnDefinition = "TEXT")
    private String deliveryAddressSnapshot;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "placed_at")
    private OffsetDateTime placedAt;

    // Delivery snapshot — stored at order-creation time so historical orders
    // are unaffected when zone pricing changes later.
    @Column(name = "delivery_pincode", length = 6)
    private String deliveryPincode;

    @Column(name = "delivery_zone_code", length = 50)
    private String deliveryZoneCode;

    @Column(name = "delivery_method", length = 20)
    private String deliveryMethod;

    @Column(name = "estimated_delivery_days")
    private Integer estimatedDeliveryDays;

    @Column(name = "estimated_delivery_minutes")
    private Integer estimatedDeliveryMinutes;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "payment_status", length = 30)
    private String paymentStatus = "pending";

    @JsonProperty("addressSnapshot")
    public Map<String, String> getAddressSnapshot() {
        if (deliveryAddressSnapshot == null || deliveryAddressSnapshot.isBlank()) return Map.of();
        try {
            return new ObjectMapper().readValue(deliveryAddressSnapshot, new TypeReference<>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }
}
