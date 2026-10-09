package in.kanchuk.repository;

import in.kanchuk.entity.VendorStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VendorStatusHistoryRepository extends JpaRepository<VendorStatusHistory, UUID> {
    List<VendorStatusHistory> findByVendorIdOrderByChangedAtDesc(UUID vendorId);
}
