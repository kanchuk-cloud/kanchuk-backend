package in.kanchuk.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "tax_rates")
public class TaxRate extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tax_category_id", nullable = false)
    private TaxCategory taxCategory;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal rate;

    @Column(name = "tax_type", nullable = false, length = 10)
    private String taxType;

    @Column(name = "state_code", length = 2)
    private String stateCode;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
