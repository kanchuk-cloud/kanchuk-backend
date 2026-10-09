package in.kanchuk.repository;

import in.kanchuk.entity.VendorPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface VendorPaymentRepository extends JpaRepository<VendorPayment, String> {

    @Query("""
        SELECT vp FROM VendorPayment vp
        JOIN FETCH vp.vendor
        LEFT JOIN FETCH vp.purchaseOrder
        WHERE vp.vendor.id = :vendorId AND vp.deletedAt IS NULL
        ORDER BY vp.createdAt DESC
        """)
    Page<VendorPayment> findByVendorIdAndDeletedAtIsNull(@Param("vendorId") UUID vendorId, Pageable pageable);

    @Query("""
        SELECT vp FROM VendorPayment vp
        JOIN FETCH vp.vendor
        LEFT JOIN FETCH vp.purchaseOrder
        WHERE vp.vendor.id = :vendorId AND vp.deletedAt IS NULL
        ORDER BY vp.createdAt DESC
        """)
    List<VendorPayment> findByVendorIdAndDeletedAtIsNull(@Param("vendorId") UUID vendorId);

    @Query("""
        SELECT vp FROM VendorPayment vp
        JOIN FETCH vp.vendor
        LEFT JOIN FETCH vp.purchaseOrder
        WHERE vp.deletedAt IS NULL
          AND (:vendorId IS NULL OR vp.vendor.id = :vendorId)
          AND (:status   IS NULL OR vp.status   = :status)
        ORDER BY vp.createdAt DESC
        """)
    Page<VendorPayment> findFiltered(
            @Param("vendorId") UUID vendorId,
            @Param("status")   String status,
            Pageable pageable);

    @Query("""
        SELECT vp FROM VendorPayment vp
        JOIN FETCH vp.vendor
        LEFT JOIN FETCH vp.purchaseOrder
        WHERE vp.deletedAt IS NULL
          AND vp.purchaseOrder.id = :purchaseOrderId
        """)
    List<VendorPayment> findByPurchaseOrderIdAndDeletedAtIsNull(@Param("purchaseOrderId") UUID purchaseOrderId);

    @Query("""
        SELECT vp FROM VendorPayment vp
        JOIN FETCH vp.vendor
        LEFT JOIN FETCH vp.purchaseOrder
        WHERE vp.deletedAt IS NULL
          AND vp.vendor.id = :vendorId
          AND vp.status NOT IN ('paid', 'failed')
          AND vp.invoiceDate <= :cutoffDate
        ORDER BY vp.invoiceDate ASC
        """)
    List<VendorPayment> findMsmeOverdue(@Param("vendorId") UUID vendorId,
                                        @Param("cutoffDate") java.time.LocalDate cutoffDate);

    @Query(value = "SELECT 'PAY' || nextval('vendor_payment_seq')", nativeQuery = true)
    String generateNextId();
}
