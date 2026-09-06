package in.kanchuk.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "variant_option_values")
public class VariantOptionValue {

    @EmbeddedId
    private VariantOptionValueId id;

    @MapsId("variantId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @MapsId("optionValueId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "option_value_id", nullable = false)
    private ProductOptionValue optionValue;

    @Embeddable
    @Getter
    @Setter
    @lombok.EqualsAndHashCode
    public static class VariantOptionValueId implements java.io.Serializable {
        @Column(name = "variant_id")
        private UUID variantId;
        @Column(name = "option_value_id")
        private UUID optionValueId;
    }
}
