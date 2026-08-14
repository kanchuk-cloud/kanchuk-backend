package in.kanchuk.repository;

import in.kanchuk.entity.ShippingMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ShippingMethodRepository extends JpaRepository<ShippingMethod, UUID> {
    Page<ShippingMethod> findByDeletedAtIsNull(Pageable pageable);
    List<ShippingMethod> findByDeletedAtIsNullAndIsActiveTrue();
}
