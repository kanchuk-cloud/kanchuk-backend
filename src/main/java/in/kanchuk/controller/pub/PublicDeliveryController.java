package in.kanchuk.controller.pub;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.dto.response.DeliveryCheckResponse;
import in.kanchuk.entity.DeliveryZone;
import in.kanchuk.entity.Pincode;
import in.kanchuk.entity.StockLocation;
import in.kanchuk.entity.StockLocationZone;
import in.kanchuk.repository.InventoryLevelRepository;
import in.kanchuk.repository.PincodeRepository;
import in.kanchuk.repository.StockLocationZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/delivery")
@RequiredArgsConstructor
public class PublicDeliveryController {

    private final PincodeRepository pincodeRepo;
    private final StockLocationZoneRepository slzRepo;
    private final InventoryLevelRepository inventoryRepo;

    @GetMapping("/check")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<DeliveryCheckResponse>> check(
            @RequestParam String pincode,
            @RequestParam(required = false) String sku) {

        String normalized = pincode == null ? "" : pincode.trim();

        if (!normalized.matches("\\d{6}")) {
            return ResponseEntity.badRequest().body(ApiResponse.ok(
                DeliveryCheckResponse.builder()
                    .serviceable(false)
                    .message("Please enter a valid 6 digit PIN code.")
                    .build()
            ));
        }

        return pincodeRepo.findById(normalized)
            .map(p -> ResponseEntity.ok(ApiResponse.ok(buildResponse(p, normalized, sku))))
            .orElseGet(() -> ResponseEntity.ok(ApiResponse.ok(
                DeliveryCheckResponse.builder()
                    .serviceable(false)
                    .pincode(normalized)
                    .message("Delivery is currently unavailable at this PIN code.")
                    .build()
            )));
    }

    private DeliveryCheckResponse buildResponse(Pincode p, String normalizedPin, String sku) {
        if (!p.isServiceable()) {
            return DeliveryCheckResponse.builder()
                .serviceable(false)
                .pincode(normalizedPin)
                .city(p.getCity())
                .state(p.getState())
                .message("Delivery is currently unavailable at this PIN code.")
                .build();
        }

        DeliveryZone zone = p.getZone();
        if (zone == null || !zone.isActive()) {
            return DeliveryCheckResponse.builder()
                .serviceable(false)
                .pincode(normalizedPin)
                .city(p.getCity())
                .state(p.getState())
                .message("Delivery is currently unavailable at this PIN code.")
                .build();
        }

        // Hub routing — only when a SKU is provided
        String hubName = null;
        String hubCity = null;
        Boolean isFallbackHub = null;

        if (sku != null && !sku.isBlank()) {
            List<StockLocationZone> zoneHubs = slzRepo.findByZoneIdOrderByPriority(zone.getId());
            boolean found = false;

            for (StockLocationZone slz : zoneHubs) {
                Long available = inventoryRepo.findAvailableQtyBySkuAndLocation(
                        sku, slz.getLocation().getId());
                if (available != null && available > 0) {
                    hubName = slz.getLocation().getName();
                    hubCity = slz.getLocation().getCity();
                    isFallbackHub = false;
                    found = true;
                    break;
                }
            }

            if (!found) {
                List<StockLocation> fallbackHubs = inventoryRepo.findHubsWithStockBySku(sku);
                if (fallbackHubs.isEmpty()) {
                    return DeliveryCheckResponse.builder()
                        .serviceable(false)
                        .pincode(normalizedPin)
                        .city(p.getCity())
                        .state(p.getState())
                        .message("This product is currently unavailable for delivery.")
                        .build();
                }
                StockLocation hub = fallbackHubs.get(0);
                hubName = hub.getName();
                hubCity = hub.getCity();
                isFallbackHub = true;
            }
        }

        DeliveryCheckResponse.DeliveryOption express = zone.isExpressAvailable()
            ? DeliveryCheckResponse.DeliveryOption.builder()
                .available(true)
                .charge(zone.getExpressDeliveryCharge())
                .estimatedMinutes(zone.getExpressDeliveryMinutes())
                .build()
            : DeliveryCheckResponse.DeliveryOption.builder()
                .available(false)
                .build();

        return DeliveryCheckResponse.builder()
            .serviceable(true)
            .pincode(normalizedPin)
            .city(p.getCity())
            .state(p.getState())
            .zone(DeliveryCheckResponse.ZoneInfo.builder()
                .code(zone.getCode())
                .name(zone.getName())
                .build())
            .standardDelivery(DeliveryCheckResponse.DeliveryOption.builder()
                .available(true)
                .charge(zone.getStandardDeliveryCharge())
                .estimatedDays(zone.getStandardDeliveryDays())
                .build())
            .expressDelivery(express)
            .codAvailable(zone.isCodAvailable())
            .hubName(hubName)
            .hubCity(hubCity)
            .isFallbackHub(isFallbackHub)
            .build();
    }
}
