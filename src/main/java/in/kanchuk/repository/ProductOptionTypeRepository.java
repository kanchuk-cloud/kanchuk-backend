package in.kanchuk.repository;

import in.kanchuk.entity.ProductOptionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductOptionTypeRepository extends JpaRepository<ProductOptionType, UUID> {

    List<ProductOptionType> findByProductSkuOrderBySortOrderAsc(String sku);
    List<ProductOptionType> findByProductIdOrderBySortOrderAsc(UUID productId);
    Optional<ProductOptionType> findByProductIdAndName(UUID productId, String name);
}
