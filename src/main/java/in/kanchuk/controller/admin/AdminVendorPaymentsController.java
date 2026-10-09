package in.kanchuk.controller.admin;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.PurchaseOrder;
import in.kanchuk.entity.Seller;
import in.kanchuk.entity.VendorPayment;
import in.kanchuk.repository.PurchaseOrderRepository;
import in.kanchuk.repository.SellerRepository;
import in.kanchuk.repository.VendorPaymentRepository;
import in.kanchuk.service.GenericAdminService;
import in.kanchuk.service.VendorService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/vendor-payments")
@RequiredArgsConstructor
public class AdminVendorPaymentsController extends GenericAdminService {

    private final VendorPaymentRepository paymentRepo;
    private final SellerRepository sellerRepo;
    private final VendorService vendorService;
    private final PurchaseOrderRepository purchaseOrderRepo;

    // ── List / Get ───────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<List<VendorPayment>>> list(
            @RequestParam(required = false) String vendorId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "20") int limit) {

        UUID vid = (vendorId != null && !vendorId.isBlank()) ? UUID.fromString(vendorId) : null;
        String statusVal = (status != null && !status.isBlank()) ? status : null;

        Page<VendorPayment> pg = paymentRepo.findFiltered(vid, statusVal, pageRequest(page, limit));
        return ResponseEntity.ok(ApiResponse.ok(pg.getContent(), buildMeta(pg, page, limit)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VendorPayment>> get(@PathVariable String id) {
        VendorPayment p = paymentRepo.findById(id)
                .filter(pay -> pay.getDeletedAt() == null)
                .orElseThrow(() -> new EntityNotFoundException("VendorPayment not found: " + id));
        return ResponseEntity.ok(ApiResponse.ok(p));
    }

    // ── Create ───────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ApiResponse<VendorPayment>> create(@RequestBody Map<String, Object> body) {
        String invoiceNumber = (String) body.get("invoiceNumber");
        if (invoiceNumber == null || invoiceNumber.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("invoice_number is required"));
        }

        UUID vendorId = UUID.fromString(body.get("vendorId").toString());
        Seller seller = sellerRepo.findById(vendorId)
                .orElseThrow(() -> new EntityNotFoundException("Vendor not found: " + vendorId));

        VendorPayment p = new VendorPayment();
        p.setVendor(seller);
        p.setInvoiceNumber(invoiceNumber);
        p.setInvoiceDate(LocalDate.parse(body.get("invoiceDate").toString()));
        if (body.get("grossAmount") != null) p.setGrossAmount(new BigDecimal(body.get("grossAmount").toString()));
        if (body.get("commissionAmount") != null) p.setCommissionAmount(new BigDecimal(body.get("commissionAmount").toString()));
        if (body.get("tdsPercent") != null) p.setTdsPercent(new BigDecimal(body.get("tdsPercent").toString()));
        if (body.get("orderId")         != null) p.setOrderId(body.get("orderId").toString());
        if (body.get("paymentMethod")   != null) p.setPaymentMethod(body.get("paymentMethod").toString());
        if (body.get("purchaseOrderId") != null) {
            UUID poId = UUID.fromString(body.get("purchaseOrderId").toString());
            PurchaseOrder po = purchaseOrderRepo.findById(poId)
                    .orElseThrow(() -> new EntityNotFoundException("PurchaseOrder not found: " + poId));
            p.setPurchaseOrder(po);
        }

        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(vendorService.createPayment(p)));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
        }
    }

    // ── State transitions ────────────────────────────────────────────────────

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<VendorPayment>> approve(
            @PathVariable String id, @RequestBody(required = false) Map<String, Object> body) {
        UUID adminId = (body != null && body.get("adminId") != null)
                ? UUID.fromString(body.get("adminId").toString()) : null;
        return ResponseEntity.ok(ApiResponse.ok(vendorService.approvePayment(id, adminId)));
    }

    @PatchMapping("/{id}/mark-paid")
    public ResponseEntity<ApiResponse<VendorPayment>> markPaid(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(vendorService.markPaid(id)));
    }

    @PatchMapping("/{id}/fail")
    public ResponseEntity<ApiResponse<VendorPayment>> fail(
            @PathVariable String id, @RequestBody(required = false) Map<String, Object> body) {
        String reason = (body != null) ? (String) body.get("failureReason") : null;
        return ResponseEntity.ok(ApiResponse.ok(vendorService.failPayment(id, reason)));
    }

    @PatchMapping("/{id}/hold")
    public ResponseEntity<ApiResponse<VendorPayment>> hold(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(vendorService.holdPayment(id)));
    }

    // ── By vendor ────────────────────────────────────────────────────────────

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<ApiResponse<List<VendorPayment>>> listByVendor(
            @PathVariable UUID vendorId,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "20") int limit) {
        Page<VendorPayment> pg = paymentRepo.findByVendorIdAndDeletedAtIsNull(vendorId, pageRequest(page, limit));
        return ResponseEntity.ok(ApiResponse.ok(pg.getContent(), buildMeta(pg, page, limit)));
    }

    @GetMapping("/by-po/{purchaseOrderId}")
    public ResponseEntity<ApiResponse<List<VendorPayment>>> listByPo(@PathVariable UUID purchaseOrderId) {
        return ResponseEntity.ok(ApiResponse.ok(paymentRepo.findByPurchaseOrderIdAndDeletedAtIsNull(purchaseOrderId)));
    }

    @GetMapping("/vendor/{vendorId}/msme-overdue")
    public ResponseEntity<ApiResponse<List<VendorPayment>>> msmeOverdue(@PathVariable UUID vendorId) {
        return ResponseEntity.ok(ApiResponse.ok(paymentRepo.findMsmeOverdue(vendorId, java.time.LocalDate.now().minusDays(45))));
    }
}
