package in.kanchuk.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "fabrics")
public class Fabric extends SoftDeleteEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "fabric_family", nullable = false, length = 100)
    private String fabricFamily;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
