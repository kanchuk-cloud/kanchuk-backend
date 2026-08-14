package in.kanchuk.controller.admin;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.FacetValue;
import in.kanchuk.repository.FacetValueRepository;
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
@RequestMapping("/api/v1/admin/facet-values")
@RequiredArgsConstructor
public class AdminFacetValueController extends GenericAdminService {

    private final FacetValueRepository repo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FacetValue>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        Page<FacetValue> pg = repo.findByDeletedAtIsNull(pageRequest(page, limit));
        return ResponseEntity.ok(ApiResponse.ok(pg.getContent(), buildMeta(pg, page, limit)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FacetValue>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(findOrThrow(repo, id, "FacetValue")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FacetValue>> create(@RequestBody FacetValue body) {
        body.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(repo.save(body)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FacetValue>> update(@PathVariable UUID id, @RequestBody Map<String, Object> fields) {
        FacetValue e = findOrThrow(repo, id, "FacetValue");
        applyPatch(e, fields);
        return ResponseEntity.ok(ApiResponse.ok(repo.save(e)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        FacetValue e = findOrThrow(repo, id, "FacetValue");
        e.softDelete();
        repo.save(e);
        return ResponseEntity.ok(ApiResponse.deleted());
    }
}
