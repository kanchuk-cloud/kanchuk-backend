package in.kanchuk.controller.admin;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.GoodsReceipt;
import in.kanchuk.entity.GoodsReceiptItem;
import in.kanchuk.entity.InventoryLevel;
import in.kanchuk.entity.PurchaseOrder;
import in.kanchuk.entity.PurchaseOrderItem;
import in.kanchuk.repository.GoodsReceiptItemRepository;
import in.kanchuk.repository.GoodsReceiptRepository;
import in.kanchuk.repository.InventoryLevelRepository;
import in.kanchuk.repository.PurchaseOrderItemRepository;
import in.kanchuk.repository.PurchaseOrderRepository;
import in.kanchuk.service.GenericAdminService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/grns")
@RequiredArgsConstructor
public class AdminGrnController extends GenericAdminService {

    private final GoodsReceiptRepository grnRepo;
    private final GoodsReceiptItemRepository grnItemRepo;
    private final PurchaseOrderRepository poRepo;
    private final PurchaseOrderItemRepository poItemRepo;
    private final InventoryLevelRepository inventoryRepo;

    // ── Get a single GRN (with items) ─────────────────────────────────────

    @GetMapping("/{grnId}")
    public ResponseEntity<ApiResponse<GoodsReceipt>> get(@PathVariable UUID grnId) {
        return ResponseEntity.ok(ApiResponse.ok(findOrThrow(grnRepo, grnId, "GoodsReceipt")));
    }

    @GetMapping("/{grnId}/items")
    public ResponseEntity<ApiResponse<List<GoodsReceiptItem>>> listItems(@PathVariable UUID grnId) {
        return ResponseEntity.ok(ApiResponse.ok(grnItemRepo.findByGoodsReceiptId(grnId)));
    }

    // ── Inspect: move to inspecting status ────────────────────────────────

    @PostMapping("/{grnId}/inspect")
    public ResponseEntity<ApiResponse<GoodsReceipt>> inspect(@PathVariable UUID grnId) {
        GoodsReceipt grn = findOrThrow(grnRepo, grnId, "GoodsReceipt");
        if (!"pending".equals(grn.getStatus())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("GRN must be in pending status to inspect"));
        }
        grn.setStatus("inspecting");
        grn.setInspectedAt(OffsetDateTime.now());
        return ResponseEntity.ok(ApiResponse.ok(grnRepo.save(grn)));
    }

    // ── Accept: accepted status + update inventory ────────────────────────

    @PostMapping("/{grnId}/accept")
    public ResponseEntity<ApiResponse<GoodsReceipt>> accept(
            @PathVariable UUID grnId,
            @RequestBody(required = false) Map<String, Object> body) {
        GoodsReceipt grn = findOrThrow(grnRepo, grnId, "GoodsReceipt");
        if (!List.of("pending", "inspecting").contains(grn.getStatus())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("GRN cannot be accepted in its current status"));
        }

        grn.setStatus("accepted");
        grn.setInspectedAt(OffsetDateTime.now());

        // Update GRN item accepted quantities if provided
        if (body != null && body.containsKey("itemUpdates")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> itemUpdates = (List<Map<String, Object>>) body.get("itemUpdates");
            for (Map<String, Object> upd : itemUpdates) {
                UUID itemId = UUID.fromString(upd.get("id").toString());
                grnItemRepo.findById(itemId).ifPresent(item -> {
                    if (upd.containsKey("quantityAccepted")) {
                        item.setQuantityAccepted(Integer.parseInt(upd.get("quantityAccepted").toString()));
                    }
                    if (upd.containsKey("quantityRejected")) {
                        item.setQuantityRejected(Integer.parseInt(upd.get("quantityRejected").toString()));
                    }
                    if (upd.containsKey("quantityDamaged")) {
                        item.setQuantityDamaged(Integer.parseInt(upd.get("quantityDamaged").toString()));
                    }
                    if (upd.containsKey("rejectionReason")) {
                        item.setRejectionReason(upd.get("rejectionReason").toString());
                    }
                    grnItemRepo.save(item);
                });
            }
        }

        // Update inventory if not already done
        if (!grn.isInventoryUpdated()) {
            updateInventory(grn);
            grn.setInventoryUpdated(true);
        }

        // Update PO quantities received and check if fully received
        updatePoQuantitiesReceived(grn.getPurchaseOrder());

        return ResponseEntity.ok(ApiResponse.ok(grnRepo.save(grn)));
    }

    // ── Reject: rejected status ────────────────────────────────────────────

    @PostMapping("/{grnId}/reject")
    public ResponseEntity<ApiResponse<GoodsReceipt>> reject(
            @PathVariable UUID grnId,
            @RequestBody(required = false) Map<String, String> body) {
        GoodsReceipt grn = findOrThrow(grnRepo, grnId, "GoodsReceipt");
        if (!List.of("pending", "inspecting").contains(grn.getStatus())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("GRN cannot be rejected in its current status"));
        }
        grn.setStatus("rejected");
        grn.setInspectedAt(OffsetDateTime.now());
        if (body != null && body.containsKey("rejectionNotes")) {
            grn.setRejectionNotes(body.get("rejectionNotes"));
        }
        return ResponseEntity.ok(ApiResponse.ok(grnRepo.save(grn)));
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private void updateInventory(GoodsReceipt grn) {
        List<GoodsReceiptItem> items = grnItemRepo.findByGoodsReceiptId(grn.getId());
        UUID locationId = grn.getLocation() != null ? grn.getLocation().getId() : null;

        for (GoodsReceiptItem grnItem : items) {
            int acceptedQty = grnItem.getQuantityAccepted();
            if (acceptedQty <= 0) continue;

            PurchaseOrderItem poItem = grnItem.getPurchaseOrderItem();
            if (poItem.getListingId() == null || locationId == null) continue;

            // Find existing inventory level for this listing + location
            inventoryRepo.findByListingIdAndLocationId(poItem.getListingId(), locationId)
                    .ifPresentOrElse(
                            level -> {
                                level.setQuantityOnHand(level.getQuantityOnHand() + acceptedQty);
                                inventoryRepo.save(level);
                            },
                            () -> {
                                // No existing record — skip (admin must create inventory level first)
                            }
                    );
        }
    }

    private void updatePoQuantitiesReceived(PurchaseOrder po) {
        List<PurchaseOrderItem> poItems = poItemRepo.findByPurchaseOrderId(po.getId());
        boolean allReceived = true;

        for (PurchaseOrderItem poItem : poItems) {
            // Sum accepted quantities across all GRN items for this PO item
            List<GoodsReceiptItem> grnItems = grnItemRepo.findByPurchaseOrderItemId(poItem.getId());
            int totalAccepted = grnItems.stream()
                    .filter(gi -> "accepted".equals(gi.getGoodsReceipt().getStatus()))
                    .mapToInt(GoodsReceiptItem::getQuantityAccepted)
                    .sum();
            poItem.setQuantityReceived(totalAccepted);
            poItemRepo.save(poItem);

            if (totalAccepted < poItem.getQuantityOrdered()) {
                allReceived = false;
            }
        }

        if (allReceived) {
            po.setStatus("received");
        } else {
            po.setStatus("partially_received");
        }
        poRepo.save(po);
    }
}
