package in.kanchuk.repository;

import in.kanchuk.entity.VendorUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VendorUserRepository extends JpaRepository<VendorUser, UUID> {
    Optional<VendorUser> findByEmailAndDeletedAtIsNull(String email);
    Page<VendorUser> findBySeller_IdAndDeletedAtIsNull(UUID sellerId, Pageable pageable);
    boolean existsByEmailAndDeletedAtIsNull(String email);
}
