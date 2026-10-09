package in.kanchuk.service;

import in.kanchuk.entity.*;
import in.kanchuk.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class VendorService {

    private static final List<String> REQUIRED_DOC_TYPES = List.of("PAN", "CANCELLED_CHEQUE", "SIGNED_AGREEMENT");
    private static final String GST_DOC_TYPE = "GST_CERTIFICATE";

    private final SellerRepository sellerRepo;
    private final VendorDocumentRepository docRepo;
    private final VendorPaymentRepository paymentRepo;
    private final VendorStatusHistoryRepository historyRepo;
    private final AdminUserRepository adminUserRepo;
    private final PurchaseOrderRepository purchaseOrderRepo;

    // ── Status transitions ───────────────────────────────────────────────────

    public Seller updateStatus(UUID vendorId, String newStatus, String reason, UUID changedByAdminId) {
        Seller seller = findSeller(vendorId);

        if ("active".equals(newStatus) && !isEligibleForActivation(vendorId)) {
            List<String> missing = getMissingVerifiedDocuments(vendorId);
            throw new IllegalStateException(
                    "Cannot activate vendor — missing/unverified documents: " + String.join(", ", missing));
        }

        String oldStatus = seller.getStatus();
        seller.setStatus(newStatus);
        seller = sellerRepo.save(seller);
        logHistory(seller, "status", oldStatus, newStatus, reason, changedByAdminId);
        return seller;
    }

    public Seller updateKycStatus(UUID vendorId, String newStatus, String reason, UUID changedByAdminId) {
        Seller seller = findSeller(vendorId);
        String oldStatus = seller.getKycStatus();
        seller.setKycStatus(newStatus);
        seller = sellerRepo.save(seller);
        logHistory(seller, "kyc_status", oldStatus, newStatus, reason, changedByAdminId);
        return seller;
    }

    // ── KYC document gate ────────────────────────────────────────────────────

    public List<String> getMissingVerifiedDocuments(UUID vendorId) {
        Seller seller = findSeller(vendorId);
        List<VendorDocument> docs = docRepo.findByVendorId(vendorId);

        List<String> required = new ArrayList<>(REQUIRED_DOC_TYPES);
        if (seller.getGstin() != null && !seller.getGstin().isBlank()) {
            required.add(GST_DOC_TYPE);
        }

        List<String> missing = new ArrayList<>();
        for (String docType : required) {
            boolean verified = docs.stream()
                    .anyMatch(d -> docType.equals(d.getDocumentType()) && "verified".equals(d.getVerificationStatus()));
            if (!verified) missing.add(docType);
        }
        return missing;
    }

    public boolean isEligibleForActivation(UUID vendorId) {
        return getMissingVerifiedDocuments(vendorId).isEmpty();
    }

    /**
     * Called after a document is verified. If all required docs are now verified,
     * auto-sets kyc_status = 'verified' on the vendor.
     */
    public void checkAndAutoVerifyKyc(UUID vendorId) {
        if (isEligibleForActivation(vendorId)) {
            Seller seller = findSeller(vendorId);
            if (!"verified".equals(seller.getKycStatus())) {
                String old = seller.getKycStatus();
                seller.setKycStatus("verified");
                sellerRepo.save(seller);
                logHistory(seller, "kyc_status", old, "verified", "Auto-verified: all required documents verified", null);
            }
        }
    }

    // ── MSME rules ───────────────────────────────────────────────────────────

    public LocalDate getMsmeDueDate(VendorPayment payment) {
        return payment.getInvoiceDate().plusDays(45);
    }

    public boolean isMsmeOverdue(VendorPayment payment) {
        return LocalDate.now().isAfter(getMsmeDueDate(payment));
    }

    // ── Payment amounts ──────────────────────────────────────────────────────

    public VendorPayment calculatePaymentAmounts(VendorPayment payment) {
        BigDecimal gross = payment.getGrossAmount() != null ? payment.getGrossAmount() : BigDecimal.ZERO;
        BigDecimal commission = payment.getCommissionAmount() != null ? payment.getCommissionAmount() : BigDecimal.ZERO;
        BigDecimal tdsPercent = payment.getTdsPercent() != null ? payment.getTdsPercent() : BigDecimal.ZERO;

        // TDS = gross × (tdsPercent / 100), rounded to 2dp
        BigDecimal tdsAmount = gross.multiply(tdsPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal netPayable = gross.subtract(commission).subtract(tdsAmount);

        payment.setTdsAmount(tdsAmount);
        payment.setNetPayable(netPayable);
        return payment;
    }

    public VendorPayment createPayment(VendorPayment payment) {
        if (payment.getInvoiceNumber() == null || payment.getInvoiceNumber().isBlank()) {
            throw new IllegalArgumentException("invoice_number is required");
        }
        String newId = paymentRepo.generateNextId();
        payment.setId(newId);
        calculatePaymentAmounts(payment);
        return paymentRepo.save(payment);
    }

    public VendorPayment approvePayment(String paymentId, UUID adminId) {
        VendorPayment p = findPayment(paymentId);
        p.setStatus("processing");
        p.setApprovedAt(OffsetDateTime.now());
        if (adminId != null) {
            adminUserRepo.findById(adminId).ifPresent(p::setApprovedBy);
        }
        VendorPayment saved = paymentRepo.save(p);
        syncPoPaymentStatus(saved);
        return saved;
    }

    public VendorPayment markPaid(String paymentId) {
        VendorPayment p = findPayment(paymentId);
        p.setStatus("paid");
        p.setPaymentDate(LocalDate.now());
        VendorPayment saved = paymentRepo.save(p);
        syncPoPaymentStatus(saved);
        return saved;
    }

    public VendorPayment failPayment(String paymentId, String failureReason) {
        VendorPayment p = findPayment(paymentId);
        p.setStatus("failed");
        p.setFailureReason(failureReason);
        VendorPayment saved = paymentRepo.save(p);
        syncPoPaymentStatus(saved);
        return saved;
    }

    public VendorPayment holdPayment(String paymentId) {
        VendorPayment p = findPayment(paymentId);
        p.setStatus("on_hold");
        VendorPayment saved = paymentRepo.save(p);
        syncPoPaymentStatus(saved);
        return saved;
    }

    private void syncPoPaymentStatus(VendorPayment payment) {
        if (payment.getPurchaseOrder() == null) return;
        UUID poId = payment.getPurchaseOrder().getId();
        PurchaseOrder po = purchaseOrderRepo.findById(poId).orElse(null);
        if (po == null) return;

        List<VendorPayment> linked = paymentRepo.findByPurchaseOrderIdAndDeletedAtIsNull(poId);
        if (linked.isEmpty()) return;

        // Exclude failed payments — they don't count toward completion
        List<VendorPayment> active = linked.stream()
                .filter(vp -> !"failed".equals(vp.getStatus()))
                .toList();

        String newStatus;
        if (active.isEmpty()) {
            newStatus = "pending"; // all failed
        } else if (active.stream().allMatch(vp -> "paid".equals(vp.getStatus()))) {
            newStatus = "paid";    // every active payment is paid
        } else if (active.stream().anyMatch(vp -> "paid".equals(vp.getStatus()))) {
            newStatus = "partial"; // some paid, some not
        } else {
            newStatus = "pending"; // none paid yet
        }
        po.setPaymentStatus(newStatus);
        purchaseOrderRepo.save(po);
    }

    public void softDeletePayment(String paymentId) {
        VendorPayment p = findPayment(paymentId);
        p.setDeletedAt(OffsetDateTime.now());
        paymentRepo.save(p);
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private Seller findSeller(UUID vendorId) {
        return sellerRepo.findById(vendorId)
                .orElseThrow(() -> new EntityNotFoundException("Vendor not found: " + vendorId));
    }

    private VendorPayment findPayment(String paymentId) {
        return paymentRepo.findById(paymentId)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new EntityNotFoundException("VendorPayment not found: " + paymentId));
    }

    private void logHistory(Seller seller, String fieldName,
                            String oldValue, String newValue,
                            String reason, UUID changedByAdminId) {
        VendorStatusHistory h = new VendorStatusHistory();
        h.setVendor(seller);
        h.setFieldName(fieldName);
        h.setOldValue(oldValue);
        h.setNewValue(newValue);
        h.setReason(reason);
        if (changedByAdminId != null) {
            adminUserRepo.findById(changedByAdminId).ifPresent(h::setChangedBy);
        }
        historyRepo.save(h);
    }
}
