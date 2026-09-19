package in.kanchuk.repository;

import in.kanchuk.entity.PurchaseOrderPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PurchaseOrderPaymentRepository extends JpaRepository<PurchaseOrderPayment, UUID> {
    List<PurchaseOrderPayment> findByPurchaseOrderId(UUID poId);
}
