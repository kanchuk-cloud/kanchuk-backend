package in.kanchuk.repository;

import in.kanchuk.entity.ProductListing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductListingRepository extends JpaRepository<ProductListing, UUID> {
    Optional<ProductListing> findFirstByVariantIdAndDeletedAtIsNull(UUID variantId);
    List<ProductListing> findByVariantIdAndDeletedAtIsNull(UUID variantId);
}
