package in.kanchuk.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "savings_edit_cards")
public class SavingsEditCard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "category_slug", nullable = false, length = 100)
    private String categorySlug;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "label_override", length = 100)
    private String labelOverride;

    @Column(name = "min_discount", nullable = false)
    private int minDiscount = 0;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
