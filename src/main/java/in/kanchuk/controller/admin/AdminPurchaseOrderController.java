package in.kanchuk.controller.admin;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.GoodsReceipt;
import in.kanchuk.entity.GoodsReceiptItem;
import in.kanchuk.entity.PurchaseOrder;
import in.kanchuk.entity.PurchaseOrderItem;
import in.kanchuk.entity.PurchaseOrderPayment;
import in.kanchuk.repository.GoodsReceiptItemRepository;
import in.kanchuk.repository.GoodsReceiptRepository;
import in.kanchuk.repository.PurchaseOrderItemRepository;
import in.kanchuk.repository.PurchaseOrderPaymentRepository;
import in.kanchuk.repository.PurchaseOrderRepository;
import in.kanchuk.service.GenericAdminService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/purchase-orders")
@RequiredArgsConstructor
public class AdminPurchaseOrderController extends GenericAdminService {

    private final PurchaseOrderRepository repo;
    private final PurchaseOrderItemRepository itemRepo;
    private final GoodsReceiptRepository grnRepo;
    private final GoodsReceiptItemRepository grnItemRepo;
    private final PurchaseOrderPaymentRepository paymentRepo;

    // ── PO CRUD ──────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<List<PurchaseOrder>>> list(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sellerId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        Page<PurchaseOrder> pg;
        if (!search.isBlank()) {
            pg = repo.findByPoNumberContainingIgnoreCase(search, pageRequest(page, limit));
        } else if (status != null && sellerId != null) {
            pg = repo.findByStatusAndSeller_Id(status, UUID.fromString(sellerId), pageRequest(page, limit));
        } else if (status != null) {
            pg = repo.findByStatus(status, pageRequest(page, limit));
        } else if (sellerId != null) {
            pg = repo.findBySeller_Id(UUID.fromString(sellerId), pageRequest(page, limit));
        } else {
            pg = repo.findAll(pageRequest(page, limit));
        }
        return ResponseEntity.ok(ApiResponse.ok(pg.getContent(), buildMeta(pg, page, limit)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PurchaseOrder>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(findOrThrow(repo, id, "PurchaseOrder")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PurchaseOrder>> create(@RequestBody PurchaseOrder body) {
        body.setId(null);
        if (body.getPoNumber() == null || body.getPoNumber().isBlank()) {
            body.setPoNumber("PO-" + System.currentTimeMillis());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(repo.save(body)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PurchaseOrder>> update(@PathVariable UUID id, @RequestBody Map<String, Object> fields) {
        PurchaseOrder e = findOrThrow(repo, id, "PurchaseOrder");
        applyPatch(e, fields);
        return ResponseEntity.ok(ApiResponse.ok(repo.save(e)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        PurchaseOrder po = findOrThrow(repo, id, "PurchaseOrder");
        po.setStatus("cancelled");
        po.setCancelledAt(OffsetDateTime.now());
        repo.save(po);
        return ResponseEntity.ok(ApiResponse.deleted());
    }

    // ── STATUS TRANSITIONS ───────────────────────────────────────────────────

    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<PurchaseOrder>> submit(@PathVariable UUID id) {
        PurchaseOrder po = findOrThrow(repo, id, "PurchaseOrder");
        po.setStatus("pending_approval");
        return ResponseEntity.ok(ApiResponse.ok(repo.save(po)));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<PurchaseOrder>> approve(@PathVariable UUID id,
            @RequestBody(required = false) Map<String, Object> body) {
        PurchaseOrder po = findOrThrow(repo, id, "PurchaseOrder");
        po.setStatus("approved");
        po.setApprovedAt(OffsetDateTime.now());
        return ResponseEntity.ok(ApiResponse.ok(repo.save(po)));
    }

    @PostMapping("/{id}/send-to-vendor")
    public ResponseEntity<ApiResponse<PurchaseOrder>> sendToVendor(@PathVariable UUID id) {
        PurchaseOrder po = findOrThrow(repo, id, "PurchaseOrder");
        po.setStatus("sent_to_vendor");
        po.setSentAt(OffsetDateTime.now());
        return ResponseEntity.ok(ApiResponse.ok(repo.save(po)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<PurchaseOrder>> cancel(@PathVariable UUID id,
            @RequestBody(required = false) Map<String, Object> body) {
        PurchaseOrder po = findOrThrow(repo, id, "PurchaseOrder");
        po.setStatus("cancelled");
        po.setCancelledAt(OffsetDateTime.now());
        if (body != null && body.containsKey("reason")) {
            po.setCancellationReason(body.get("reason").toString());
        }
        return ResponseEntity.ok(ApiResponse.ok(repo.save(po)));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<ApiResponse<PurchaseOrder>> close(@PathVariable UUID id) {
        PurchaseOrder po = findOrThrow(repo, id, "PurchaseOrder");
        po.setStatus("closed");
        return ResponseEntity.ok(ApiResponse.ok(repo.save(po)));
    }

    @PostMapping("/{id}/reopen")
    public ResponseEntity<ApiResponse<PurchaseOrder>> reopen(@PathVariable UUID id) {
        PurchaseOrder po = findOrThrow(repo, id, "PurchaseOrder");
        if (!"closed".equals(po.getStatus())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Only closed POs can be reopened"));
        }
        List<in.kanchuk.entity.PurchaseOrderItem> items = itemRepo.findByPurchaseOrderId(id);
        boolean hasPending = items.stream().anyMatch(i -> i.getQuantityReceived() < i.getQuantityOrdered());
        po.setStatus(hasPending ? "partially_received" : "received");
        return ResponseEntity.ok(ApiResponse.ok(repo.save(po)));
    }

    // ── LINE ITEMS ────────────────────────────────────────────────────────────

    @GetMapping("/{id}/items")
    public ResponseEntity<ApiResponse<List<PurchaseOrderItem>>> listItems(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(itemRepo.findByPurchaseOrderId(id)));
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<ApiResponse<PurchaseOrderItem>> addItem(@PathVariable UUID id,
            @RequestBody PurchaseOrderItem body) {
        PurchaseOrder po = findOrThrow(repo, id, "PurchaseOrder");
        body.setId(null);
        body.setPurchaseOrder(po);
        PurchaseOrderItem saved = itemRepo.save(body);
        recalcTotals(po);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(saved));
    }

    @PutMapping("/{id}/items/{itemId}")
    public ResponseEntity<ApiResponse<PurchaseOrderItem>> updateItem(@PathVariable UUID id,
            @PathVariable UUID itemId, @RequestBody Map<String, Object> fields) {
        PurchaseOrderItem item = itemRepo.findById(itemId)
                .filter(i -> i.getPurchaseOrder().getId().equals(id))
                .orElseThrow(() -> new EntityNotFoundException("PurchaseOrderItem not found: " + itemId));
        applyPatch(item, fields);
        PurchaseOrderItem saved = itemRepo.save(item);
        recalcTotals(findOrThrow(repo, id, "PurchaseOrder"));
        return ResponseEntity.ok(ApiResponse.ok(saved));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable UUID id, @PathVariable UUID itemId) {
        itemRepo.deleteById(itemId);
        recalcTotals(findOrThrow(repo, id, "PurchaseOrder"));
        return ResponseEntity.ok(ApiResponse.deleted());
    }

    // ── GRN ──────────────────────────────────────────────────────────────────

    @GetMapping("/{id}/grns")
    public ResponseEntity<ApiResponse<List<GoodsReceipt>>> listGrns(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(grnRepo.findByPurchaseOrderId(id)));
    }

    @PostMapping("/{id}/grns")
    public ResponseEntity<ApiResponse<GoodsReceipt>> createGrn(@PathVariable UUID id,
            @RequestBody GoodsReceipt body) {
        PurchaseOrder po = findOrThrow(repo, id, "PurchaseOrder");
        body.setId(null);
        body.setPurchaseOrder(po);
        if (body.getGrnNumber() == null || body.getGrnNumber().isBlank()) {
            body.setGrnNumber("GRN-" + System.currentTimeMillis());
        }
        GoodsReceipt saved = grnRepo.save(body);
        // Update PO status to partially_received or received
        po.setStatus("partially_received");
        repo.save(po);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(saved));
    }

    @PutMapping("/{id}/grns/{grnId}")
    public ResponseEntity<ApiResponse<GoodsReceipt>> updateGrn(@PathVariable UUID id,
            @PathVariable UUID grnId, @RequestBody Map<String, Object> fields) {
        GoodsReceipt grn = grnRepo.findById(grnId)
                .filter(g -> g.getPurchaseOrder().getId().equals(id))
                .orElseThrow(() -> new EntityNotFoundException("GoodsReceipt not found: " + grnId));
        applyPatch(grn, fields);
        return ResponseEntity.ok(ApiResponse.ok(grnRepo.save(grn)));
    }

    @GetMapping("/{id}/grns/{grnId}/items")
    public ResponseEntity<ApiResponse<List<GoodsReceiptItem>>> listGrnItems(@PathVariable UUID id,
            @PathVariable UUID grnId) {
        return ResponseEntity.ok(ApiResponse.ok(grnItemRepo.findByGoodsReceiptId(grnId)));
    }

    @PostMapping("/{id}/grns/{grnId}/items")
    public ResponseEntity<ApiResponse<GoodsReceiptItem>> addGrnItem(@PathVariable UUID id,
            @PathVariable UUID grnId, @RequestBody GoodsReceiptItem body) {
        GoodsReceipt grn = grnRepo.findById(grnId)
                .orElseThrow(() -> new EntityNotFoundException("GoodsReceipt not found: " + grnId));
        body.setId(null);
        body.setGoodsReceipt(grn);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(grnItemRepo.save(body)));
    }

    // ── PAYMENTS ─────────────────────────────────────────────────────────────

    @GetMapping("/{id}/payments")
    public ResponseEntity<ApiResponse<List<PurchaseOrderPayment>>> listPayments(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(paymentRepo.findByPurchaseOrderId(id)));
    }

    @PostMapping("/{id}/payments")
    public ResponseEntity<ApiResponse<PurchaseOrderPayment>> addPayment(@PathVariable UUID id,
            @RequestBody PurchaseOrderPayment body) {
        PurchaseOrder po = findOrThrow(repo, id, "PurchaseOrder");
        body.setId(null);
        body.setPurchaseOrder(po);
        PurchaseOrderPayment saved = paymentRepo.save(body);
        updatePaymentStatus(po);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(saved));
    }

    @DeleteMapping("/{id}/payments/{paymentId}")
    public ResponseEntity<ApiResponse<Void>> deletePayment(@PathVariable UUID id, @PathVariable UUID paymentId) {
        paymentRepo.deleteById(paymentId);
        updatePaymentStatus(findOrThrow(repo, id, "PurchaseOrder"));
        return ResponseEntity.ok(ApiResponse.deleted());
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private void recalcTotals(PurchaseOrder po) {
        List<PurchaseOrderItem> items = itemRepo.findByPurchaseOrderId(po.getId());
        java.math.BigDecimal subtotal = items.stream()
                .map(i -> i.getTotalCost() != null ? i.getTotalCost() : java.math.BigDecimal.ZERO)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        po.setSubtotal(subtotal);
        po.setTotalAmount(subtotal
                .add(po.getTaxAmount() != null ? po.getTaxAmount() : java.math.BigDecimal.ZERO)
                .add(po.getShippingCharges() != null ? po.getShippingCharges() : java.math.BigDecimal.ZERO)
                .subtract(po.getDiscountAmount() != null ? po.getDiscountAmount() : java.math.BigDecimal.ZERO));
        po.setTotalItems(items.size());
        repo.save(po);
    }

    private void updatePaymentStatus(PurchaseOrder po) {
        List<PurchaseOrderPayment> payments = paymentRepo.findByPurchaseOrderId(po.getId());
        java.math.BigDecimal paid = payments.stream()
                .map(p -> p.getAmount() != null ? p.getAmount() : java.math.BigDecimal.ZERO)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        java.math.BigDecimal total = po.getTotalAmount() != null ? po.getTotalAmount() : java.math.BigDecimal.ZERO;
        if (paid.compareTo(java.math.BigDecimal.ZERO) == 0) {
            po.setPaymentStatus("pending");
        } else if (paid.compareTo(total) >= 0) {
            po.setPaymentStatus("paid");
        } else {
            po.setPaymentStatus("partial");
        }
        repo.save(po);
    }
}
