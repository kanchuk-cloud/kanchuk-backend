package in.kanchuk.controller.admin;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.DeliveryZone;
import in.kanchuk.repository.DeliveryZoneRepository;
import in.kanchuk.service.GenericAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/delivery/zones")
@RequiredArgsConstructor
public class AdminDeliveryZoneController extends GenericAdminService {

    private final DeliveryZoneRepository repo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DeliveryZone>>> list(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        Page<DeliveryZone> pg = search.isBlank()
            ? repo.findAll(pageRequest(page, limit))
            : repo.findByNameContainingIgnoreCase(search, pageRequest(page, limit));
        return ResponseEntity.ok(ApiResponse.ok(pg.getContent(), buildMeta(pg, page, limit)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DeliveryZone>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(findOrThrow(repo, id, "DeliveryZone")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DeliveryZone>> create(@RequestBody DeliveryZone body) {
        body.setId(null);
        if (body.getCode() != null) body.setCode(body.getCode().toUpperCase().trim());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(repo.save(body)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DeliveryZone>> update(
            @PathVariable UUID id, @RequestBody Map<String, Object> fields) {
        DeliveryZone zone = findOrThrow(repo, id, "DeliveryZone");
        applyPatch(zone, fields);
        return ResponseEntity.ok(ApiResponse.ok(repo.save(zone)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable UUID id) {
        DeliveryZone zone = findOrThrow(repo, id, "DeliveryZone");
        zone.setActive(false);
        repo.save(zone);
        return ResponseEntity.ok(ApiResponse.deleted());
    }
}
