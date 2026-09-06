package in.kanchuk.controller.admin;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.dto.response.StockLocationZoneDto;
import in.kanchuk.entity.DeliveryZone;
import in.kanchuk.entity.StockLocation;
import in.kanchuk.entity.StockLocationZone;
import in.kanchuk.entity.StockLocationZoneId;
import in.kanchuk.repository.DeliveryZoneRepository;
import in.kanchuk.repository.StockLocationRepository;
import in.kanchuk.repository.StockLocationZoneRepository;
import in.kanchuk.service.GenericAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/stock-locations/{locationId}/zones")
@RequiredArgsConstructor
public class AdminStockLocationZoneController extends GenericAdminService {

    private final StockLocationZoneRepository slzRepo;
    private final StockLocationRepository locationRepo;
    private final DeliveryZoneRepository zoneRepo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<StockLocationZoneDto>>> list(
            @PathVariable UUID locationId) {
        findOrThrow(locationRepo, locationId, "StockLocation");
        List<StockLocationZoneDto> result = slzRepo
            .findByLocationIdOrderByPriority(locationId)
            .stream()
            .map(slz -> new StockLocationZoneDto(
                slz.getId().getLocationId(),
                slz.getId().getZoneId(),
                slz.getPriority(),
                slz.getZone()))
            .toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StockLocationZoneDto>> assign(
            @PathVariable UUID locationId,
            @RequestBody Map<String, Object> body) {

        StockLocation location = findOrThrow(locationRepo, locationId, "StockLocation");
        UUID zoneId = UUID.fromString(body.get("zoneId").toString());
        int priority = body.containsKey("priority")
            ? Integer.parseInt(body.get("priority").toString())
            : 1;

        DeliveryZone zone = findOrThrow(zoneRepo, zoneId, "DeliveryZone");

        StockLocationZoneId compositeId = new StockLocationZoneId(locationId, zoneId);
        StockLocationZone slz = slzRepo.findById(compositeId).orElseGet(() -> {
            StockLocationZone n = new StockLocationZone();
            n.setId(compositeId);
            n.setLocation(location);
            n.setZone(zone);
            return n;
        });
        slz.setPriority(priority);
        slzRepo.save(slz);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(
            new StockLocationZoneDto(locationId, zoneId, slz.getPriority(), zone)));
    }

    @DeleteMapping("/{zoneId}")
    public ResponseEntity<ApiResponse<Void>> remove(
            @PathVariable UUID locationId,
            @PathVariable UUID zoneId) {
        findOrThrow(locationRepo, locationId, "StockLocation");
        slzRepo.deleteByLocationIdAndZoneId(locationId, zoneId);
        return ResponseEntity.ok(ApiResponse.deleted());
    }
}
