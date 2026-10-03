package in.kanchuk.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "testimonials")
public class Testimonial extends BaseEntity {

    @Column(name = "customer_name", nullable = false, length = 120)
    private String customerName;

    @Column(length = 80)
    private String city;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String quote;

    @Column(nullable = false)
    private short rating = 5;

    @Column(name = "product_name", length = 200)
    private String productName;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = true;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;
}
