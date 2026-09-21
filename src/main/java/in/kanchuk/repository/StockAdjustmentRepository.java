package in.kanchuk.repository;

import in.kanchuk.entity.StockAdjustment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface StockAdjustmentRepository extends JpaRepository<StockAdjustment, UUID> {

    @Query("""
        SELECT sa FROM StockAdjustment sa
        WHERE (:listingId IS NULL OR sa.listing.id = :listingId)
          AND (:locationId IS NULL OR sa.location.id = :locationId)
          AND (:reasonCode IS NULL OR sa.reasonCode = :reasonCode)
        ORDER BY sa.createdAt DESC
        """)
    Page<StockAdjustment> findFiltered(
            @Param("listingId") UUID listingId,
            @Param("locationId") UUID locationId,
            @Param("reasonCode") String reasonCode,
            Pageable pageable);
}
