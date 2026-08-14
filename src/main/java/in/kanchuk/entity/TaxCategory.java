package in.kanchuk.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tax_categories")
public class TaxCategory extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "hsn_code", length = 8)
    private String hsnCode;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
