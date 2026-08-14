package in.kanchuk.repository;

import in.kanchuk.entity.PurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {
    Page<PurchaseOrder> findAll(Pageable pageable);
    Page<PurchaseOrder> findByPoNumberContainingIgnoreCase(String poNumber, Pageable pageable);
}
