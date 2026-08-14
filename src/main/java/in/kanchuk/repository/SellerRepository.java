package in.kanchuk.repository;

import in.kanchuk.entity.Seller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SellerRepository extends JpaRepository<Seller, UUID> {
    Page<Seller> findByDeletedAtIsNull(Pageable pageable);
    Page<Seller> findByDeletedAtIsNullAndNameContainingIgnoreCase(String name, Pageable pageable);
}
