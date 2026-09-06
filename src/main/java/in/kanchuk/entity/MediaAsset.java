package in.kanchuk.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "media_assets")
public class MediaAsset extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false, length = 50)
    private String subcategory;

    @Column(nullable = false, length = 255)
    private String filename;

    @Column(name = "original_name", length = 255)
    private String originalName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "content_type", length = 100)
    private String contentType;
}
