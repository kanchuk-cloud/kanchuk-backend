package in.kanchuk.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "shipping_methods")
public class ShippingMethod extends SoftDeleteEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "carrier", length = 100)
    private String carrier;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "free_above", precision = 10, scale = 2)
    private BigDecimal freeAbove;

    @Column(name = "estimated_days")
    private Integer estimatedDays;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;
}
