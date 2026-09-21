package in.kanchuk.repository;

import in.kanchuk.entity.StockTransfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface StockTransferRepository extends JpaRepository<StockTransfer, UUID> {

    @Query("""
        SELECT st FROM StockTransfer st
        WHERE (:status IS NULL OR st.status = :status)
          AND (:locationId IS NULL OR st.fromLocation.id = :locationId OR st.toLocation.id = :locationId)
        ORDER BY st.createdAt DESC
        """)
    Page<StockTransfer> findFiltered(
            @Param("status") String status,
            @Param("locationId") UUID locationId,
            Pageable pageable);
}
