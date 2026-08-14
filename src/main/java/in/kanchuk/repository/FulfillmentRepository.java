package in.kanchuk.repository;

import in.kanchuk.entity.Fulfillment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FulfillmentRepository extends JpaRepository<Fulfillment, UUID> {
    Page<Fulfillment> findAll(Pageable pageable);
    List<Fulfillment> findByOrderId(UUID orderId);
}
