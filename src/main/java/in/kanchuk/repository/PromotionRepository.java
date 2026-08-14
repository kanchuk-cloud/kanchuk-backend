package in.kanchuk.repository;

import in.kanchuk.entity.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PromotionRepository extends JpaRepository<Promotion, UUID> {
    Page<Promotion> findByDeletedAtIsNull(Pageable pageable);
    Page<Promotion> findByDeletedAtIsNullAndNameContainingIgnoreCase(String name, Pageable pageable);
    Optional<Promotion> findByCouponCodeAndDeletedAtIsNull(String couponCode);
}
