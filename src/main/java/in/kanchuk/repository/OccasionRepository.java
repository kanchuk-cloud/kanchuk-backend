package in.kanchuk.repository;

import in.kanchuk.entity.Occasion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OccasionRepository extends JpaRepository<Occasion, UUID> {
    Page<Occasion> findByDeletedAtIsNull(Pageable pageable);
    Page<Occasion> findByDeletedAtIsNullAndNameContainingIgnoreCase(String name, Pageable pageable);
    List<Occasion> findByDeletedAtIsNullAndIsActiveTrueOrderBySortOrderAsc();
}
