package in.kanchuk.repository;

import in.kanchuk.entity.VendorDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VendorDocumentRepository extends JpaRepository<VendorDocument, UUID> {
    List<VendorDocument> findByVendorId(UUID vendorId);
    List<VendorDocument> findByVendorIdAndVerificationStatus(UUID vendorId, String verificationStatus);
}
