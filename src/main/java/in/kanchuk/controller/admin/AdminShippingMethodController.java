package in.kanchuk.controller.admin;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.ShippingMethod;
import in.kanchuk.repository.ShippingMethodRepository;
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
@RequestMapping("/api/v1/admin/shipping-methods")
@RequiredArgsConstructor
public class AdminShippingMethodController extends GenericAdminService {

    private final ShippingMethodRepository repo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ShippingMethod>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        Page<ShippingMethod> pg = repo.findByDeletedAtIsNull(pageRequest(page, limit));
        return ResponseEntity.ok(ApiResponse.ok(pg.getContent(), buildMeta(pg, page, limit)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ShippingMethod>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(findOrThrow(repo, id, "ShippingMethod")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ShippingMethod>> create(@RequestBody ShippingMethod body) {
        body.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(repo.save(body)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ShippingMethod>> update(@PathVariable UUID id, @RequestBody Map<String, Object> fields) {
        ShippingMethod e = findOrThrow(repo, id, "ShippingMethod");
        applyPatch(e, fields);
        return ResponseEntity.ok(ApiResponse.ok(repo.save(e)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        ShippingMethod e = findOrThrow(repo, id, "ShippingMethod");
        e.softDelete();
        repo.save(e);
        return ResponseEntity.ok(ApiResponse.deleted());
    }
}
