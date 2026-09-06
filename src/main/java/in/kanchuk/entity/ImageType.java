package in.kanchuk.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "image_types")
public class ImageType extends BaseEntity {

    @Column(name = "label", nullable = false, length = 100)
    private String label;

    @Column(name = "value", nullable = false, unique = true, length = 100)
    private String value;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
