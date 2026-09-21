package in.kanchuk.controller.admin;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.InventoryLevel;
import in.kanchuk.repository.InventoryLevelRepository;
import in.kanchuk.service.GenericAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/inventory/levels")
@RequiredArgsConstructor
public class AdminInventoryLevelsController extends GenericAdminService {

    private final InventoryLevelRepository repo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<InventoryLevel>>> list(
            @RequestParam(required = false) String listingId,
            @RequestParam(required = false) String locationId,
            @RequestParam(required = false) String lowStock,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int limit) {

        Page<InventoryLevel> pg;

        if (listingId != null && locationId != null) {
            pg = repo.findByListingIdAndLocationId(
                    UUID.fromString(listingId),
                    UUID.fromString(locationId),
                    pageRequest(page, limit));
        } else if (listingId != null) {
            pg = repo.findByListingId(UUID.fromString(listingId), pageRequest(page, limit));
        } else if (locationId != null) {
            pg = repo.findByLocationId(UUID.fromString(locationId), pageRequest(page, limit));
        } else if ("true".equals(lowStock)) {
            pg = repo.findLowStock(pageRequest(page, limit));
        } else {
            pg = repo.findAllEagerPaged(pageRequest(page, limit));
        }

        return ResponseEntity.ok(ApiResponse.ok(pg.getContent(), buildMeta(pg, page, limit)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryLevel>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(findOrThrow(repo, id, "InventoryLevel")));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryLevel>> update(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> body) {
        InventoryLevel level = findOrThrow(repo, id, "InventoryLevel");
        applyPatch(level, body);
        return ResponseEntity.ok(ApiResponse.ok(repo.save(level)));
    }
}
