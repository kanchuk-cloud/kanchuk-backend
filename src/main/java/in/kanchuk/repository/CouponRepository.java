package in.kanchuk.repository;

import in.kanchuk.entity.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CouponRepository extends JpaRepository<Coupon, UUID> {
    Page<Coupon> findByCodeContainingIgnoreCase(String code, Pageable pageable);
    Optional<Coupon> findByCodeIgnoreCase(String code);

    @Query("SELECT c FROM Coupon c WHERE c.isActive = true AND c.expiryDate >= :today ORDER BY c.createdAt DESC")
    List<Coupon> findActiveAndNotExpired(LocalDate today);
}
