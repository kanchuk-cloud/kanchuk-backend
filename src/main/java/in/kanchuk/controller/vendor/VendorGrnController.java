package in.kanchuk.controller.vendor;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.GoodsReceipt;
import in.kanchuk.entity.GoodsReceiptItem;
import in.kanchuk.entity.PurchaseOrder;
import in.kanchuk.entity.PurchaseOrderItem;
import in.kanchuk.entity.VendorUser;
import in.kanchuk.repository.GoodsReceiptItemRepository;
import in.kanchuk.repository.GoodsReceiptRepository;
import in.kanchuk.repository.PurchaseOrderItemRepository;
import in.kanchuk.repository.PurchaseOrderRepository;
import in.kanchuk.repository.VendorUserRepository;
import in.kanchuk.service.GenericAdminService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vendor")
@RequiredArgsConstructor
public class VendorGrnController extends GenericAdminService {

    private final GoodsReceiptRepository grnRepo;
    private final GoodsReceiptItemRepository grnItemRepo;
    private final PurchaseOrderRepository poRepo;
    private final PurchaseOrderItemRepository poItemRepo;
    private final VendorUserRepository vendorUserRepo;

    private UUID currentSellerId(Authentication auth) {
        return UUID.fromString((String) auth.getCredentials());
    }

    private VendorUser currentVendorUser(Authentication auth) {
        String email = (String) auth.getPrincipal();
        return vendorUserRepo.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new EntityNotFoundException("Vendor user not found"));
    }

    // ── Create GRN for a PO ────────────────────────────────────────────────

    @PostMapping("/purchase-orders/{poId}/grns")
    public ResponseEntity<ApiResponse<GoodsReceipt>> createGrn(
            @PathVariable UUID poId,
            @RequestBody GoodsReceipt body,
            Authentication auth) {
        UUID sellerId = currentSellerId(auth);
        PurchaseOrder po = findOrThrow(poRepo, poId, "PurchaseOrder");

        if (!po.getSeller().getId().equals(sellerId)) {
            throw new EntityNotFoundException("PurchaseOrder not found: " + poId);
        }

        if (!List.of("sent_to_vendor", "partially_received").contains(po.getStatus())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("GRN can only be created when PO is sent to vendor or partially received"));
        }

        body.setId(null);
        body.setPurchaseOrder(po);
        body.setStatus("pending");
        body.setSubmittedByVendorUser(currentVendorUser(auth));
        if (body.getGrnNumber() == null || body.getGrnNumber().isBlank()) {
            body.setGrnNumber("GRN-" + System.currentTimeMillis());
        }

        GoodsReceipt saved = grnRepo.save(body);

        // Move PO to partially_received
        if ("sent_to_vendor".equals(po.getStatus())) {
            po.setStatus("partially_received");
            poRepo.save(po);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(saved));
    }

    // ── Add items to GRN ───────────────────────────────────────────────────

    @PostMapping("/grns/{grnId}/items")
    public ResponseEntity<ApiResponse<GoodsReceiptItem>> addGrnItem(
            @PathVariable UUID grnId,
            @RequestBody GoodsReceiptItem body,
            Authentication auth) {
        UUID sellerId = currentSellerId(auth);
        GoodsReceipt grn = findOrThrow(grnRepo, grnId, "GoodsReceipt");

        if (!grn.getPurchaseOrder().getSeller().getId().equals(sellerId)) {
            throw new EntityNotFoundException("GoodsReceipt not found: " + grnId);
        }

        if (!"pending".equals(grn.getStatus())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Cannot add items to a GRN that is not pending"));
        }

        // Validate quantities
        PurchaseOrderItem poItem = body.getPurchaseOrderItem();
        if (poItem != null && poItem.getId() != null) {
            PurchaseOrderItem existing = poItemRepo.findById(poItem.getId())
                    .orElseThrow(() -> new EntityNotFoundException("PurchaseOrderItem not found"));

            // Reject duplicate: same PO item already in this GRN
            boolean alreadyAdded = grnItemRepo.findByGoodsReceiptId(grnId).stream()
                    .anyMatch(gi -> gi.getPurchaseOrderItem().getId().equals(existing.getId()));
            if (alreadyAdded) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("This item is already added to the GRN. Edit the existing entry instead."));
            }

            int remaining = existing.getQuantityOrdered() - existing.getQuantityReceived();
            if (body.getQuantityReceived() < 1) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Quantity received must be at least 1"));
            }
            if (body.getQuantityReceived() > remaining) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Quantity received (" + body.getQuantityReceived()
                                + ") exceeds remaining ordered quantity (" + remaining + ")"));
            }
        }

        body.setId(null);
        body.setGoodsReceipt(grn);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(grnItemRepo.save(body)));
    }

    // ── Get GRN items ──────────────────────────────────────────────────────

    @GetMapping("/grns/{grnId}/items")
    public ResponseEntity<ApiResponse<List<GoodsReceiptItem>>> listGrnItems(
            @PathVariable UUID grnId, Authentication auth) {
        UUID sellerId = currentSellerId(auth);
        GoodsReceipt grn = findOrThrow(grnRepo, grnId, "GoodsReceipt");
        if (!grn.getPurchaseOrder().getSeller().getId().equals(sellerId)) {
            throw new EntityNotFoundException("GoodsReceipt not found: " + grnId);
        }
        return ResponseEntity.ok(ApiResponse.ok(grnItemRepo.findByGoodsReceiptId(grnId)));
    }

    // ── Get single GRN ─────────────────────────────────────────────────────

    @GetMapping("/grns/{grnId}")
    public ResponseEntity<ApiResponse<GoodsReceipt>> getGrn(
            @PathVariable UUID grnId, Authentication auth) {
        UUID sellerId = currentSellerId(auth);
        GoodsReceipt grn = findOrThrow(grnRepo, grnId, "GoodsReceipt");
        if (!grn.getPurchaseOrder().getSeller().getId().equals(sellerId)) {
            throw new EntityNotFoundException("GoodsReceipt not found: " + grnId);
        }
        return ResponseEntity.ok(ApiResponse.ok(grn));
    }

    // ── Update GRN header (only when pending) ──────────────────────────────

    @PutMapping("/grns/{grnId}")
    public ResponseEntity<ApiResponse<GoodsReceipt>> updateGrn(
            @PathVariable UUID grnId,
            @RequestBody Map<String, Object> body,
            Authentication auth) {
        UUID sellerId = currentSellerId(auth);
        GoodsReceipt grn = findOrThrow(grnRepo, grnId, "GoodsReceipt");

        if (!grn.getPurchaseOrder().getSeller().getId().equals(sellerId)) {
            throw new EntityNotFoundException("GoodsReceipt not found: " + grnId);
        }
        if (!"pending".equals(grn.getStatus())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Only pending GRNs can be edited"));
        }

        if (body.containsKey("carrierName"))   grn.setCarrierName((String) body.get("carrierName"));
        if (body.containsKey("vehicleNumber")) grn.setVehicleNumber((String) body.get("vehicleNumber"));
        if (body.containsKey("invoiceNumber")) grn.setInvoiceNumber((String) body.get("invoiceNumber"));
        if (body.containsKey("notes"))         grn.setNotes((String) body.get("notes"));
        if (body.containsKey("invoiceDate") && body.get("invoiceDate") != null) {
            grn.setInvoiceDate(java.time.LocalDate.parse((String) body.get("invoiceDate")));
        }

        return ResponseEntity.ok(ApiResponse.ok(grnRepo.save(grn)));
    }

    // ── Update GRN item quantity (only when pending) ────────────────────────

    @PutMapping("/grns/{grnId}/items/{itemId}")
    public ResponseEntity<ApiResponse<GoodsReceiptItem>> updateGrnItem(
            @PathVariable UUID grnId,
            @PathVariable UUID itemId,
            @RequestBody Map<String, Object> body,
            Authentication auth) {
        UUID sellerId = currentSellerId(auth);
        GoodsReceipt grn = findOrThrow(grnRepo, grnId, "GoodsReceipt");

        if (!grn.getPurchaseOrder().getSeller().getId().equals(sellerId)) {
            throw new EntityNotFoundException("GoodsReceipt not found: " + grnId);
        }
        if (!"pending".equals(grn.getStatus())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Only pending GRNs can be edited"));
        }

        GoodsReceiptItem item = grnItemRepo.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("GoodsReceiptItem not found: " + itemId));

        if (!item.getGoodsReceipt().getId().equals(grnId)) {
            throw new EntityNotFoundException("GoodsReceiptItem not found: " + itemId);
        }

        if (body.containsKey("quantityReceived")) {
            int qty = ((Number) body.get("quantityReceived")).intValue();
            PurchaseOrderItem poItem = item.getPurchaseOrderItem();
            int alreadyReceived = poItem.getQuantityReceived() - item.getQuantityReceived();
            int remaining = poItem.getQuantityOrdered() - alreadyReceived;
            if (qty < 1 || qty > remaining) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Quantity must be between 1 and " + remaining));
            }
            item.setQuantityReceived(qty);
        }

        return ResponseEntity.ok(ApiResponse.ok(grnItemRepo.save(item)));
    }

    // ── Delete GRN item (only when pending) ────────────────────────────────

    @DeleteMapping("/grns/{grnId}/items/{itemId}")
    public ResponseEntity<ApiResponse<Void>> deleteGrnItem(
            @PathVariable UUID grnId,
            @PathVariable UUID itemId,
            Authentication auth) {
        UUID sellerId = currentSellerId(auth);
        GoodsReceipt grn = findOrThrow(grnRepo, grnId, "GoodsReceipt");

        if (!grn.getPurchaseOrder().getSeller().getId().equals(sellerId)) {
            throw new EntityNotFoundException("GoodsReceipt not found: " + grnId);
        }
        if (!"pending".equals(grn.getStatus())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Only pending GRNs can be edited"));
        }

        grnItemRepo.deleteById(itemId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
