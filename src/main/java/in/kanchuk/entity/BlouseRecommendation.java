package in.kanchuk.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "blouse_recommendations")
public class BlouseRecommendation extends BaseEntity {

    @Column(name = "applies_fabric", length = 100)
    private String appliesFabric;

    @Column(name = "applies_pattern", length = 100)
    private String appliesPattern;

    @Column(name = "applies_occasion", length = 100)
    private String appliesOccasion;

    @Column(name = "style_name", nullable = false, length = 200)
    private String styleName;

    @Column(nullable = false, length = 100)
    private String neckline;

    @Column(nullable = false, length = 100)
    private String sleeve;

    @Column(nullable = false, length = 100)
    private String embellishment;

    @Column(name = "fabric_suggestion", nullable = false, length = 200)
    private String fabricSuggestion;

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
