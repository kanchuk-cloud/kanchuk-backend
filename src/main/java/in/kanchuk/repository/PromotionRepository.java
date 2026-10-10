package in.kanchuk.repository;

import in.kanchuk.entity.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface PromotionRepository extends JpaRepository<Promotion, UUID> {
    Page<Promotion> findByDeletedAtIsNull(Pageable pageable);
    Page<Promotion> findByDeletedAtIsNullAndNameContainingIgnoreCase(String name, Pageable pageable);
    Optional<Promotion> findByCouponCodeAndDeletedAtIsNull(String couponCode);

    @Modifying
    @Transactional
    @Query("UPDATE Promotion p SET p.usedCount = p.usedCount + 1 WHERE p.id = :id AND (p.usageLimit IS NULL OR p.usedCount < p.usageLimit)")
    int incrementUsedCountIfAllowed(UUID id);
}
