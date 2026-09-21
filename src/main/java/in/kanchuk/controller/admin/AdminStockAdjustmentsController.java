package in.kanchuk.controller.admin;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.AdminUser;
import in.kanchuk.entity.InventoryLevel;
import in.kanchuk.entity.StockAdjustment;
import in.kanchuk.repository.AdminUserRepository;
import in.kanchuk.repository.InventoryLevelRepository;
import in.kanchuk.repository.StockAdjustmentRepository;
import in.kanchuk.service.GenericAdminService;
import in.kanchuk.service.InventoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/inventory/adjustments")
@RequiredArgsConstructor
public class AdminStockAdjustmentsController extends GenericAdminService {

    private final StockAdjustmentRepository repo;
    private final InventoryLevelRepository inventoryRepo;
    private final AdminUserRepository adminUserRepo;
    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<StockAdjustment>>> list(
            @RequestParam(required = false) String listingId,
            @RequestParam(required = false) String locationId,
            @RequestParam(required = false) String reasonCode,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int limit) {

        UUID lid = listingId != null ? UUID.fromString(listingId) : null;
        UUID locId = locationId != null ? UUID.fromString(locationId) : null;

        Page<StockAdjustment> pg = repo.findFiltered(lid, locId, reasonCode, pageRequest(page, limit));
        return ResponseEntity.ok(ApiResponse.ok(pg.getContent(), buildMeta(pg, page, limit)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StockAdjustment>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(findOrThrow(repo, id, "StockAdjustment")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StockAdjustment>> create(
            @RequestBody Map<String, Object> body,
            Authentication auth) {

        UUID listingId = UUID.fromString(body.get("listingId").toString());
        UUID locationId = UUID.fromString(body.get("locationId").toString());
        int qtyChange = Integer.parseInt(body.get("qtyChange").toString());
        String reasonCode = body.get("reasonCode").toString();
        String notes = body.containsKey("notes") ? body.get("notes").toString() : null;

        AdminUser performer = resolveAdminUser(auth);

        InventoryLevel level = inventoryRepo.findByListingIdAndLocationId(listingId, locationId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No inventory level found for listing " + listingId + " at location " + locationId));

        // Capture before/after and associations BEFORE the service call (which commits its own tx)
        var listing = level.getListing();
        var location = level.getLocation();
        int before = level.getQuantityOnHand();
        int after = Math.max(0, before + qtyChange);

        // Use InventoryService for atomic update + movement record
        inventoryService.adjustStock(listingId, locationId, qtyChange,
                "adjustment", "stock_adjustment", null, notes, performer);

        // Record the adjustment
        StockAdjustment adj = new StockAdjustment();
        adj.setListing(listing);
        adj.setLocation(location);
        adj.setReasonCode(reasonCode);
        adj.setQtyBefore(before);
        adj.setQtyChange(qtyChange);
        adj.setQtyAfter(after);
        adj.setNotes(notes);
        adj.setAdjustedBy(performer);

        return ResponseEntity.ok(ApiResponse.ok(repo.save(adj)));
    }

    private AdminUser resolveAdminUser(Authentication auth) {
        if (auth == null) return null;
        String email = auth.getName();
        return adminUserRepo.findByEmailAndDeletedAtIsNull(email).orElse(null);
    }
}
