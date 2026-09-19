package in.kanchuk.repository;

import in.kanchuk.entity.GoodsReceiptItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GoodsReceiptItemRepository extends JpaRepository<GoodsReceiptItem, UUID> {
    List<GoodsReceiptItem> findByGoodsReceiptId(UUID grnId);
    List<GoodsReceiptItem> findByPurchaseOrderItemId(UUID poItemId);
}
