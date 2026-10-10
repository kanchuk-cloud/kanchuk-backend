package in.kanchuk.repository;

import in.kanchuk.entity.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CouponRepository extends JpaRepository<Coupon, UUID> {
    Page<Coupon> findByCodeContainingIgnoreCase(String code, Pageable pageable);
    Optional<Coupon> findByCodeIgnoreCase(String code);

    @Query("SELECT c FROM Coupon c WHERE c.isActive = true AND c.expiryDate >= :today AND (c.validFrom IS NULL OR c.validFrom <= :today) AND (c.usageLimit IS NULL OR c.usageCount < c.usageLimit) ORDER BY c.createdAt DESC")
    List<Coupon> findActiveAndNotExpired(LocalDate today);

    @Modifying
    @Transactional
    @Query("UPDATE Coupon c SET c.usageCount = c.usageCount + 1 WHERE c.id = :id AND (c.usageLimit IS NULL OR c.usageCount < c.usageLimit)")
    int incrementUsageCountIfAllowed(UUID id);
}
