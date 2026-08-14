package in.kanchuk.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "size_charts")
public class SizeChart extends SoftDeleteEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 20)
    private String unit;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "chart_data", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> chartData;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
