package in.kanchuk.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "suit_styling_recommendations")
public class SuitStylingRecommendation extends BaseEntity {

    @Column(name = "applies_fabric", length = 100)
    private String appliesFabric;

    @Column(name = "applies_pattern", length = 100)
    private String appliesPattern;

    @Column(name = "applies_occasion", length = 100)
    private String appliesOccasion;

    @Column(name = "style_name", nullable = false, length = 200)
    private String styleName;

    @Column(name = "bottom_style", nullable = false, length = 100)
    private String bottomStyle;

    @Column(name = "kurta_style", nullable = false, length = 100)
    private String kurtaStyle;

    @Column(name = "dupatta_style", nullable = false, length = 100)
    private String dupattaStyle;

    @Column(name = "reason_text", nullable = false, columnDefinition = "TEXT")
    private String reasonText;

    @Column(length = 50)
    private String tag;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
