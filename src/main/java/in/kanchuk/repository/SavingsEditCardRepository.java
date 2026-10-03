package in.kanchuk.repository;

import in.kanchuk.entity.SavingsEditCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SavingsEditCardRepository extends JpaRepository<SavingsEditCard, UUID> {
    Page<SavingsEditCard> findAll(Pageable pageable);
    List<SavingsEditCard> findByIsActiveTrueOrderBySortOrderAsc();
}
