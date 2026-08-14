package in.kanchuk.repository;

import in.kanchuk.entity.StockLocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StockLocationRepository extends JpaRepository<StockLocation, UUID> {
    Page<StockLocation> findByDeletedAtIsNull(Pageable pageable);
    Page<StockLocation> findByDeletedAtIsNullAndNameContainingIgnoreCase(String name, Pageable pageable);
}
