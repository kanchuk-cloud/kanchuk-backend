package in.kanchuk.repository;

import in.kanchuk.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    Page<Product> findByDeletedAtIsNull(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Product> searchProducts(@Param("search") String search, Pageable pageable);

    Optional<Product> findBySkuAndDeletedAtIsNull(String sku);

    Page<Product> findByCategorySlugAndDeletedAtIsNullAndIsActiveTrue(String categorySlug, Pageable pageable);
    Page<Product> findByDeletedAtIsNullAndIsActiveTrue(Pageable pageable);
}
