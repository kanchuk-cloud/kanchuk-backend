package in.kanchuk.repository;

import in.kanchuk.entity.Offer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OfferRepository extends JpaRepository<Offer, UUID> {
    Page<Offer> findByDeletedAtIsNull(Pageable pageable);
    Page<Offer> findByDeletedAtIsNullAndTitleContainingIgnoreCase(String title, Pageable pageable);
}
