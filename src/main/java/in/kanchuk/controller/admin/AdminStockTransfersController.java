package in.kanchuk.controller.admin;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.AdminUser;
import in.kanchuk.entity.ProductListing;
import in.kanchuk.entity.StockLocation;
import in.kanchuk.entity.StockTransfer;
import in.kanchuk.entity.StockTransferItem;
import in.kanchuk.repository.AdminUserRepository;
import in.kanchuk.repository.ProductListingRepository;
import in.kanchuk.repository.StockLocationRepository;
import in.kanchuk.repository.StockTransferItemRepository;
import in.kanchuk.repository.StockTransferRepository;
import in.kanchuk.service.GenericAdminService;
import in.kanchuk.service.InventoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/inventory/transfers")
@RequiredArgsConstructor
public class AdminStockTransfersController extends GenericAdminService {

    private final StockTransferRepository repo;
    private final StockTransferItemRepository itemRepo;
    private final StockLocationRepository locationRepo;
    private final ProductListingRepository listingRepo;
    private final AdminUserRepository adminUserRepo;
    private final InventoryService inventoryService;

    // ── List transfers ────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<List<StockTransfer>>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String locationId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {

        UUID locId = locationId != null ? UUID.fromString(locationId) : null;
        Page<StockTransfer> pg = repo.findFiltered(status, locId, pageRequest(page, limit));
        return ResponseEntity.ok(ApiResponse.ok(pg.getContent(), buildMeta(pg, page, limit)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StockTransfer>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(findOrThrow(repo, id, "StockTransfer")));
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<ApiResponse<List<StockTransferItem>>> listItems(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(itemRepo.findByTransferId(id)));
    }

    // ── Create transfer ───────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ApiResponse<StockTransfer>> create(
            @RequestBody Map<String, Object> body,
            Authentication auth) {

        UUID fromLocId = UUID.fromString(body.get("fromLocationId").toString());
        UUID toLocId = UUID.fromString(body.get("toLocationId").toString());

        StockLocation from = locationRepo.findById(fromLocId)
                .orElseThrow(() -> new EntityNotFoundException("Location not found: " + fromLocId));
        StockLocation to = locationRepo.findById(toLocId)
                .orElseThrow(() -> new EntityNotFoundException("Location not found: " + toLocId));

        StockTransfer transfer = new StockTransfer();
        transfer.setTransferNumber("TRF-" + DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(OffsetDateTime.now()));
        transfer.setFromLocation(from);
        transfer.setToLocation(to);
        transfer.setStatus("draft");
        if (body.containsKey("notes")) transfer.setNotes(body.get("notes").toString());
        transfer.setCreatedBy(resolveAdminUser(auth));

        return ResponseEntity.ok(ApiResponse.ok(repo.save(transfer)));
    }

    // ── Add item to transfer ─────────────────────────────────────────────────

    @PostMapping("/{id}/items")
    public ResponseEntity<ApiResponse<StockTransferItem>> addItem(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> body) {

        StockTransfer transfer = findOrThrow(repo, id, "StockTransfer");
        if (!"draft".equals(transfer.getStatus())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Items can only be added to draft transfers"));
        }

        UUID listingId = UUID.fromString(body.get("listingId").toString());
        ProductListing listing = listingRepo.findById(listingId)
                .orElseThrow(() -> new EntityNotFoundException("Listing not found: " + listingId));

        StockTransferItem item = new StockTransferItem();
        item.setTransfer(transfer);
        item.setListing(listing);
        item.setQtyRequested(Integer.parseInt(body.get("quantityRequested").toString()));
        item.setQtyTransferred(0);

        return ResponseEntity.ok(ApiResponse.ok(itemRepo.save(item)));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<ApiResponse<Void>> removeItem(
            @PathVariable UUID id,
            @PathVariable UUID itemId) {

        StockTransfer transfer = findOrThrow(repo, id, "StockTransfer");
        if (!"draft".equals(transfer.getStatus())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Items can only be removed from draft transfers"));
        }
        itemRepo.deleteById(itemId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // ── Status transitions ────────────────────────────────────────────────────

    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<StockTransfer>> submit(@PathVariable UUID id) {
        StockTransfer transfer = findOrThrow(repo, id, "StockTransfer");
        if (!"draft".equals(transfer.getStatus())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Only draft transfers can be submitted"));
        }
        transfer.setStatus("in_transit");
        return ResponseEntity.ok(ApiResponse.ok(repo.save(transfer)));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<StockTransfer>> complete(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, Object> body,
            Authentication auth) {

        StockTransfer transfer = findOrThrow(repo, id, "StockTransfer");
        if (!"in_transit".equals(transfer.getStatus())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Only in-transit transfers can be completed"));
        }

        List<StockTransferItem> items = itemRepo.findByTransferId(id);
        AdminUser performer = resolveAdminUser(auth);

        // Apply qty updates if provided
        if (body != null && body.containsKey("itemUpdates")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> updates = (List<Map<String, Object>>) body.get("itemUpdates");
            for (Map<String, Object> upd : updates) {
                UUID itemId = UUID.fromString(upd.get("id").toString());
                items.stream().filter(i -> i.getId().equals(itemId)).findFirst().ifPresent(item -> {
                    item.setQtyTransferred(Integer.parseInt(upd.get("quantityTransferred").toString()));
                    itemRepo.save(item);
                });
            }
            items = itemRepo.findByTransferId(id); // reload updated items
        }

        // Move stock: deduct from source, add to destination
        for (StockTransferItem item : items) {
            int qty = item.getQtyTransferred() > 0 ? item.getQtyTransferred() : item.getQtyRequested();
            UUID listingId = item.getListing().getId();

            inventoryService.adjustStock(listingId, transfer.getFromLocation().getId(),
                    -qty, "transfer_out", "stock_transfer", transfer.getId(),
                    "Transfer " + transfer.getTransferNumber(), performer);

            inventoryService.adjustStock(listingId, transfer.getToLocation().getId(),
                    qty, "transfer_in", "stock_transfer", transfer.getId(),
                    "Transfer " + transfer.getTransferNumber(), performer);
        }

        transfer.setStatus("completed");
        transfer.setCompletedAt(OffsetDateTime.now());
        transfer.setCompletedBy(performer);

        return ResponseEntity.ok(ApiResponse.ok(repo.save(transfer)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<StockTransfer>> cancel(@PathVariable UUID id) {
        StockTransfer transfer = findOrThrow(repo, id, "StockTransfer");
        if (List.of("completed", "cancelled").contains(transfer.getStatus())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Transfer cannot be cancelled in its current status"));
        }
        transfer.setStatus("cancelled");
        return ResponseEntity.ok(ApiResponse.ok(repo.save(transfer)));
    }

    private AdminUser resolveAdminUser(Authentication auth) {
        if (auth == null) return null;
        return adminUserRepo.findByEmailAndDeletedAtIsNull(auth.getName()).orElse(null);
    }
}
