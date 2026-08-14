package in.kanchuk.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "product_option_values")
public class ProductOptionValue extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "option_type_id", nullable = false)
    private ProductOptionType optionType;

    @Column(nullable = false, length = 100)
    private String value;

    @Column(nullable = false, length = 10)
    private String code;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;
}
