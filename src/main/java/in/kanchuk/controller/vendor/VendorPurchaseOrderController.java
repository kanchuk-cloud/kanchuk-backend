package in.kanchuk.controller.vendor;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.GoodsReceipt;
import in.kanchuk.entity.PurchaseOrder;
import in.kanchuk.entity.PurchaseOrderItem;
import in.kanchuk.entity.PurchaseOrderPayment;
import in.kanchuk.repository.GoodsReceiptRepository;
import in.kanchuk.repository.PurchaseOrderItemRepository;
import in.kanchuk.repository.PurchaseOrderPaymentRepository;
import in.kanchuk.repository.PurchaseOrderRepository;
import in.kanchuk.service.GenericAdminService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vendor/purchase-orders")
@RequiredArgsConstructor
public class VendorPurchaseOrderController extends GenericAdminService {

    private final PurchaseOrderRepository poRepo;
    private final PurchaseOrderItemRepository itemRepo;
    private final GoodsReceiptRepository grnRepo;
    private final PurchaseOrderPaymentRepository paymentRepo;

    private UUID currentSellerId(Authentication auth) {
        return UUID.fromString((String) auth.getCredentials());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PurchaseOrder>>> list(
            Authentication auth,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        UUID sellerId = currentSellerId(auth);
        Page<PurchaseOrder> pg;
        if (status != null) {
            pg = poRepo.findByStatusAndSeller_Id(status, sellerId, pageRequest(page, limit));
        } else {
            pg = poRepo.findBySeller_Id(sellerId, pageRequest(page, limit));
        }
        return ResponseEntity.ok(ApiResponse.ok(pg.getContent(), buildMeta(pg, page, limit)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PurchaseOrder>> get(@PathVariable UUID id, Authentication auth) {
        UUID sellerId = currentSellerId(auth);
        PurchaseOrder po = findOrThrow(poRepo, id, "PurchaseOrder");
        if (!po.getSeller().getId().equals(sellerId)) {
            throw new EntityNotFoundException("PurchaseOrder not found: " + id);
        }
        return ResponseEntity.ok(ApiResponse.ok(po));
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<ApiResponse<List<PurchaseOrderItem>>> listItems(
            @PathVariable UUID id, Authentication auth) {
        UUID sellerId = currentSellerId(auth);
        PurchaseOrder po = findOrThrow(poRepo, id, "PurchaseOrder");
        if (!po.getSeller().getId().equals(sellerId)) {
            throw new EntityNotFoundException("PurchaseOrder not found: " + id);
        }
        return ResponseEntity.ok(ApiResponse.ok(itemRepo.findByPurchaseOrderId(id)));
    }

    @GetMapping("/{id}/grns")
    public ResponseEntity<ApiResponse<List<GoodsReceipt>>> listGrns(
            @PathVariable UUID id, Authentication auth) {
        UUID sellerId = currentSellerId(auth);
        PurchaseOrder po = findOrThrow(poRepo, id, "PurchaseOrder");
        if (!po.getSeller().getId().equals(sellerId)) {
            throw new EntityNotFoundException("PurchaseOrder not found: " + id);
        }
        return ResponseEntity.ok(ApiResponse.ok(grnRepo.findByPurchaseOrderId(id)));
    }

    @GetMapping("/{id}/payments")
    public ResponseEntity<ApiResponse<List<PurchaseOrderPayment>>> listPayments(
            @PathVariable UUID id, Authentication auth) {
        UUID sellerId = currentSellerId(auth);
        PurchaseOrder po = findOrThrow(poRepo, id, "PurchaseOrder");
        if (!po.getSeller().getId().equals(sellerId)) {
            throw new EntityNotFoundException("PurchaseOrder not found: " + id);
        }
        return ResponseEntity.ok(ApiResponse.ok(paymentRepo.findByPurchaseOrderId(id)));
    }
}
