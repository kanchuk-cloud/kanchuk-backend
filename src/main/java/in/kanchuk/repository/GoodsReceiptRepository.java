package in.kanchuk.repository;

import in.kanchuk.entity.GoodsReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, UUID> {

    @Query("SELECT g FROM GoodsReceipt g LEFT JOIN FETCH g.purchaseOrder po LEFT JOIN FETCH po.seller WHERE g.purchaseOrder.id = :poId")
    List<GoodsReceipt> findByPurchaseOrderId(@Param("poId") UUID poId);

    @Query("SELECT g FROM GoodsReceipt g LEFT JOIN FETCH g.purchaseOrder po LEFT JOIN FETCH po.seller WHERE g.id = :id")
    Optional<GoodsReceipt> findByIdWithPo(@Param("id") UUID id);
}
