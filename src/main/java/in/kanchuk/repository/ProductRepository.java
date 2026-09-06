package in.kanchuk.repository;

import in.kanchuk.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    @EntityGraph(attributePaths = {"category", "subCategory"})
    Page<Product> findByDeletedAtIsNull(Pageable pageable);

    @EntityGraph(attributePaths = {"category", "subCategory"})
    @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Product> searchProducts(@Param("search") String search, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "subCategory", "fabric", "images"})
    Optional<Product> findWithRelationsById(UUID id);

    @EntityGraph(attributePaths = {"category", "subCategory", "fabric", "images"})
    Optional<Product> findBySkuAndDeletedAtIsNull(String sku);

    @EntityGraph(attributePaths = {"category", "subCategory", "fabric"})
    Page<Product> findByCategorySlugAndDeletedAtIsNullAndIsActiveTrue(String categorySlug, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "subCategory", "fabric"})
    Page<Product> findByDeletedAtIsNullAndIsActiveTrue(Pageable pageable);

    @EntityGraph(attributePaths = {"category", "subCategory", "fabric"})
    Page<Product> findByGenderAndDeletedAtIsNullAndIsActiveTrue(String gender, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "subCategory", "fabric"})
    Page<Product> findByGenderAndIsNewTrueAndDeletedAtIsNullAndIsActiveTrue(String gender, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "subCategory", "fabric"})
    Page<Product> findByGenderAndCategorySlugAndDeletedAtIsNullAndIsActiveTrue(String gender, String categorySlug, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "subCategory", "fabric"})
    Page<Product> findByIsNewTrueAndDeletedAtIsNullAndIsActiveTrue(Pageable pageable);

    @EntityGraph(attributePaths = {"category", "subCategory"})
    Page<Product> findByDeletedAtIsNullAndCategory_Id(UUID categoryId, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "subCategory"})
    @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL AND p.category.id = :categoryId AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Product> searchProductsByCategory(@Param("categoryId") UUID categoryId, @Param("search") String search, Pageable pageable);

    @Query("SELECT p.sku FROM Product p WHERE p.sku LIKE :prefix%")
    List<String> findSkusByPrefix(@Param("prefix") String prefix);
}
