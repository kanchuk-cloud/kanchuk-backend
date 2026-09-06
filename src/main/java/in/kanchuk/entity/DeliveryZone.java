package in.kanchuk.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "delivery_zones")
public class DeliveryZone extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "standard_delivery_charge", nullable = false, precision = 10, scale = 2)
    private BigDecimal standardDeliveryCharge = new BigDecimal("49.00");

    @Column(name = "express_delivery_charge", nullable = false, precision = 10, scale = 2)
    private BigDecimal expressDeliveryCharge = new BigDecimal("99.00");

    @Column(name = "standard_delivery_days", nullable = false)
    private int standardDeliveryDays = 5;

    @Column(name = "express_delivery_minutes", nullable = false)
    private int expressDeliveryMinutes = 120;

    @Column(name = "express_available", nullable = false)
    private boolean expressAvailable = false;

    @Column(name = "cod_available", nullable = false)
    private boolean codAvailable = true;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
