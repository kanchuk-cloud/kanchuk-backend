package in.kanchuk.repository;

import in.kanchuk.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {
    List<ProductVariant> findByProductIdAndDeletedAtIsNullOrderByCreatedAtAsc(UUID productId);
    Optional<ProductVariant> findBySkuAndDeletedAtIsNull(String sku);
}
