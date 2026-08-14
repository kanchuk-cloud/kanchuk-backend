package in.kanchuk.controller.admin;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.Occasion;
import in.kanchuk.repository.OccasionRepository;
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
@RequestMapping("/api/v1/admin/occasions")
@RequiredArgsConstructor
public class AdminOccasionController extends GenericAdminService {

    private final OccasionRepository repo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Occasion>>> list(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        Page<Occasion> pg = search.isBlank()
                ? repo.findByDeletedAtIsNull(pageRequest(page, limit))
                : repo.findByDeletedAtIsNullAndNameContainingIgnoreCase(search, pageRequest(page, limit));
        return ResponseEntity.ok(ApiResponse.ok(pg.getContent(), buildMeta(pg, page, limit)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Occasion>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(findOrThrow(repo, id, "Occasion")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Occasion>> create(@RequestBody Occasion body) {
        body.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(repo.save(body)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Occasion>> update(@PathVariable UUID id, @RequestBody Map<String, Object> fields) {
        Occasion e = findOrThrow(repo, id, "Occasion");
        applyPatch(e, fields);
        return ResponseEntity.ok(ApiResponse.ok(repo.save(e)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        Occasion e = findOrThrow(repo, id, "Occasion");
        e.softDelete();
        repo.save(e);
        return ResponseEntity.ok(ApiResponse.deleted());
    }
}
