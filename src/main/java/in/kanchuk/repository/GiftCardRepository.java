package in.kanchuk.repository;

import in.kanchuk.entity.GiftCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GiftCardRepository extends JpaRepository<GiftCard, UUID> {
    Page<GiftCard> findAll(Pageable pageable);
    Optional<GiftCard> findByCode(String code);
}
