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
@Table(name = "mid_banners")
public class MidBanner {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(nullable = false, length = 200)
    private String title = "";

    @Column(nullable = false, length = 200)
    private String highlight = "";

    @Column(nullable = false, length = 200)
    private String tagline = "";

    @Column(name = "cta_text", nullable = false, length = 100)
    private String ctaText = "";

    @Column(name = "cta_link", nullable = false, length = 500)
    private String ctaLink = "";

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
