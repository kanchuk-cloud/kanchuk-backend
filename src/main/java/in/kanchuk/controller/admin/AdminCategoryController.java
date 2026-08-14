package in.kanchuk.controller.admin;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.Category;
import in.kanchuk.repository.CategoryRepository;
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
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController extends GenericAdminService {

    private final CategoryRepository repo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> list(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        Page<Category> pg = search.isBlank()
                ? repo.findByDeletedAtIsNull(pageRequest(page, limit))
                : repo.findByDeletedAtIsNullAndNameContainingIgnoreCase(search, pageRequest(page, limit));
        return ResponseEntity.ok(ApiResponse.ok(pg.getContent(), buildMeta(pg, page, limit)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(findOrThrow(repo, id, "Category")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Category>> create(@RequestBody Category body) {
        body.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(repo.save(body)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> update(@PathVariable UUID id, @RequestBody Map<String, Object> fields) {
        Category e = findOrThrow(repo, id, "Category");
        applyPatch(e, fields);
        return ResponseEntity.ok(ApiResponse.ok(repo.save(e)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        Category e = findOrThrow(repo, id, "Category");
        e.softDelete();
        repo.save(e);
        return ResponseEntity.ok(ApiResponse.deleted());
    }
}
