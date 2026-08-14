package in.kanchuk.controller.admin;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.TaxCategory;
import in.kanchuk.repository.TaxCategoryRepository;
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
@RequestMapping("/api/v1/admin/tax-categories")
@RequiredArgsConstructor
public class AdminTaxCategoryController extends GenericAdminService {

    private final TaxCategoryRepository repo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TaxCategory>>> list(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        Page<TaxCategory> pg = search.isBlank()
                ? repo.findAll(pageRequest(page, limit))
                : repo.findByNameContainingIgnoreCase(search, pageRequest(page, limit));
        return ResponseEntity.ok(ApiResponse.ok(pg.getContent(), buildMeta(pg, page, limit)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaxCategory>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(findOrThrow(repo, id, "TaxCategory")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TaxCategory>> create(@RequestBody TaxCategory body) {
        body.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(repo.save(body)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaxCategory>> update(@PathVariable UUID id, @RequestBody Map<String, Object> fields) {
        TaxCategory e = findOrThrow(repo, id, "TaxCategory");
        applyPatch(e, fields);
        return ResponseEntity.ok(ApiResponse.ok(repo.save(e)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        repo.deleteById(id);
        return ResponseEntity.ok(ApiResponse.deleted());
    }
}
