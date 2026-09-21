package in.kanchuk.controller.admin;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.StockMovement;
import in.kanchuk.repository.StockMovementRepository;
import in.kanchuk.service.GenericAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/inventory/movements")
@RequiredArgsConstructor
public class AdminStockMovementsController extends GenericAdminService {

    private final StockMovementRepository repo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<StockMovement>>> list(
            @RequestParam(required = false) String listingId,
            @RequestParam(required = false) String locationId,
            @RequestParam(required = false) String movementType,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int limit) {

        UUID lid = listingId != null ? UUID.fromString(listingId) : null;
        UUID locId = locationId != null ? UUID.fromString(locationId) : null;
        OffsetDateTime fromDt = from != null ? OffsetDateTime.parse(from) : OffsetDateTime.parse("2000-01-01T00:00:00Z");
        OffsetDateTime toDt   = to   != null ? OffsetDateTime.parse(to)   : OffsetDateTime.now().plusYears(100);

        Page<StockMovement> pg = repo.findFiltered(lid, locId, movementType, fromDt, toDt, pageRequest(page, limit));
        return ResponseEntity.ok(ApiResponse.ok(pg.getContent(), buildMeta(pg, page, limit)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StockMovement>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(findOrThrow(repo, id, "StockMovement")));
    }
}
