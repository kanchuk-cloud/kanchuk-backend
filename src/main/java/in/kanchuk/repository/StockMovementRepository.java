package in.kanchuk.repository;

import in.kanchuk.entity.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {

    @Query("""
        SELECT sm FROM StockMovement sm
        WHERE (:listingId IS NULL OR sm.listing.id = :listingId)
          AND (:locationId IS NULL OR sm.location.id = :locationId)
          AND (:movementType IS NULL OR sm.movementType = :movementType)
          AND sm.createdAt >= :from
          AND sm.createdAt <= :to
        ORDER BY sm.createdAt DESC
        """)
    Page<StockMovement> findFiltered(
            @Param("listingId") UUID listingId,
            @Param("locationId") UUID locationId,
            @Param("movementType") String movementType,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to,
            Pageable pageable);
}
