package in.kanchuk.repository;

import in.kanchuk.entity.PurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {

    @Query("SELECT po FROM PurchaseOrder po LEFT JOIN FETCH po.seller ORDER BY po.createdAt DESC")
    Page<PurchaseOrder> findAll(Pageable pageable);

    @Query("SELECT po FROM PurchaseOrder po LEFT JOIN FETCH po.seller WHERE LOWER(po.poNumber) LIKE LOWER(CONCAT('%', :q, '%')) ORDER BY po.createdAt DESC")
    Page<PurchaseOrder> findByPoNumberContainingIgnoreCase(@Param("q") String q, Pageable pageable);

    @Query("SELECT po FROM PurchaseOrder po LEFT JOIN FETCH po.seller WHERE po.status = :status ORDER BY po.createdAt DESC")
    Page<PurchaseOrder> findByStatus(@Param("status") String status, Pageable pageable);

    @Query("SELECT po FROM PurchaseOrder po LEFT JOIN FETCH po.seller WHERE po.seller.id = :sellerId ORDER BY po.createdAt DESC")
    Page<PurchaseOrder> findBySeller_Id(@Param("sellerId") UUID sellerId, Pageable pageable);

    @Query("SELECT po FROM PurchaseOrder po LEFT JOIN FETCH po.seller WHERE po.status = :status AND po.seller.id = :sellerId ORDER BY po.createdAt DESC")
    Page<PurchaseOrder> findByStatusAndSeller_Id(@Param("status") String status, @Param("sellerId") UUID sellerId, Pageable pageable);

    @Query("SELECT po FROM PurchaseOrder po LEFT JOIN FETCH po.seller WHERE po.id = :id")
    Optional<PurchaseOrder> findByIdWithSeller(@Param("id") UUID id);
}
