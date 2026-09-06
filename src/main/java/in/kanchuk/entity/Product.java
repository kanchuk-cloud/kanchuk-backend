package in.kanchuk.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "products")
public class Product extends SoftDeleteEntity {

    @Column(nullable = false, length = 100)
    private String sku;

    @Column(name = "supplier_sku", length = 100)
    private String supplierSku;

    @Column(length = 255)
    private String slug;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 150)
    private String brand;

    @Column(name = "short_description", columnDefinition = "TEXT")
    private String shortDescription;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 20)
    private String gender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_category_id")
    private SubCategory subCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fabric_id")
    private Fabric fabric;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_category_id")
    private TaxCategory taxCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designer_id")
    private Designer designer;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(length = 50)
    private String pattern;

    @Column(length = 50)
    private String sleeve;

    @Column(name = "craft_type", length = 100)
    private String craftType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "occasions", columnDefinition = "jsonb")
    private List<String> occasions;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", columnDefinition = "jsonb")
    private List<String> tags;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "care_instructions", columnDefinition = "jsonb")
    private List<String> careInstructions;

    @Column(name = "return_policy", columnDefinition = "TEXT")
    private String returnPolicy;

    @Column(name = "swatch_available", nullable = false)
    private boolean swatchAvailable = false;

    @Column(name = "swatch_price", nullable = false)
    private BigDecimal swatchPrice = BigDecimal.ZERO;

    @Column(name = "rating", nullable = false)
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "review_count", nullable = false)
    private int reviewCount = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "color_families", columnDefinition = "jsonb")
    private List<String> colorFamilies;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attributes", columnDefinition = "jsonb")
    private Map<String, String> attributes;

    @Column(name = "is_handloom", nullable = false)
    private boolean isHandloom = false;

    @Column(name = "is_designer", nullable = false)
    private boolean isDesigner = false;

    @Column(name = "is_new", nullable = false)
    private boolean isNew = false;

    @Column(name = "is_bestseller", nullable = false)
    private boolean isBestseller = false;

    @Column(name = "is_trending", nullable = false)
    private boolean isTrending = false;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @Column(name = "meta_title", length = 70)
    private String metaTitle;

    @Column(name = "meta_description", length = 160)
    private String metaDescription;

    @Column(name = "og_image_url")
    private String ogImageUrl;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<ProductImage> images = new ArrayList<>();

    @Formula("(SELECT MIN(pl.price) FROM product_variants pv " +
             "JOIN product_listings pl ON pl.variant_id = pv.id " +
             "WHERE pv.product_id = id " +
             "AND pv.deleted_at IS NULL " +
             "AND pl.deleted_at IS NULL " +
             "AND pl.is_active = true " +
             "AND pl.price > 0)")
    private BigDecimal minPrice;

    @Formula("(SELECT pl.compare_at_price FROM product_variants pv " +
             "JOIN product_listings pl ON pl.variant_id = pv.id " +
             "WHERE pv.product_id = id " +
             "AND pv.deleted_at IS NULL " +
             "AND pl.deleted_at IS NULL " +
             "AND pl.is_active = true " +
             "AND pl.price > 0 " +
             "ORDER BY pl.price ASC LIMIT 1)")
    private BigDecimal compareAtPrice;
}
