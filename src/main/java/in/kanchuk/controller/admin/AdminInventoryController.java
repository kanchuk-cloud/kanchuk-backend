package in.kanchuk.controller.admin;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.InventoryLevel;
import in.kanchuk.entity.ProductListing;
import in.kanchuk.entity.StockLocation;
import in.kanchuk.repository.InventoryLevelRepository;
import in.kanchuk.repository.ProductListingRepository;
import in.kanchuk.repository.StockLocationRepository;
import in.kanchuk.service.GenericAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/inventory")
@RequiredArgsConstructor
public class AdminInventoryController extends GenericAdminService {

    private final InventoryLevelRepository repo;
    private final ProductListingRepository listingRepo;
    private final StockLocationRepository locationRepo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<InventoryLevel>>> list(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) UUID locationId,
            @RequestParam(required = false) UUID categoryId) {
        List<InventoryLevel> results = locationId != null
                ? repo.findAllEagerByLocation(search, locationId, categoryId)
                : repo.findAllEager(search, categoryId);
        return ResponseEntity.ok(ApiResponse.ok(results));
    }

    @GetMapping("/by-product/{productId}")
    public ResponseEntity<ApiResponse<List<InventoryLevel>>> listByProduct(@PathVariable UUID productId) {
        return ResponseEntity.ok(ApiResponse.ok(repo.findByProductId(productId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryLevel>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(findOrThrow(repo, id, "InventoryLevel")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InventoryLevel>> create(@RequestBody Map<String, Object> body) {
        UUID listingId  = UUID.fromString(body.get("listingId").toString());
        UUID locationId = UUID.fromString(body.get("locationId").toString());

        ProductListing listing  = listingRepo.findById(listingId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Listing not found: " + listingId));
        StockLocation  location = locationRepo.findById(locationId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Location not found: " + locationId));

        InventoryLevel il = new InventoryLevel();
        il.setListing(listing);
        il.setLocation(location);
        il.setQuantityOnHand(body.containsKey("quantityOnHand")
                ? Integer.parseInt(body.get("quantityOnHand").toString()) : 0);
        il.setQuantityReserved(body.containsKey("quantityReserved")
                ? Integer.parseInt(body.get("quantityReserved").toString()) : 0);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(repo.save(il)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryLevel>> update(@PathVariable UUID id, @RequestBody Map<String, Object> fields) {
        InventoryLevel e = findOrThrow(repo, id, "InventoryLevel");
        applyPatch(e, fields);
        return ResponseEntity.ok(ApiResponse.ok(repo.save(e)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        repo.deleteById(id);
        return ResponseEntity.ok(ApiResponse.deleted());
    }
}
