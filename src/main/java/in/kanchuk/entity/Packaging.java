package in.kanchuk.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "packaging")
public class Packaging extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(name = "packed_by", length = 100)
    private String packedBy;

    @Column(name = "package_type", length = 50)
    private String packageType;

    @Column(name = "weight_grams")
    private Integer weightGrams;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dimensions_cm", columnDefinition = "jsonb")
    private String dimensionsCm;

    @Column(nullable = false, length = 20)
    private String status = "pending";

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "packed_at")
    private OffsetDateTime packedAt;
}
