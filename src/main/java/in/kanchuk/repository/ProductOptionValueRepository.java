package in.kanchuk.repository;

import in.kanchuk.entity.ProductOptionValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductOptionValueRepository extends JpaRepository<ProductOptionValue, UUID> {
    long countByOptionTypeId(UUID optionTypeId);
}
