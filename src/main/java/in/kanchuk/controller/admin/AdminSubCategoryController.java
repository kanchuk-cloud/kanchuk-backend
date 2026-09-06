package in.kanchuk.controller.admin;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.Category;
import in.kanchuk.entity.SubCategory;
import in.kanchuk.repository.CategoryRepository;
import in.kanchuk.repository.SubCategoryRepository;
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
@RequestMapping("/api/v1/admin/sub-categories")
@RequiredArgsConstructor
public class AdminSubCategoryController extends GenericAdminService {

    private final SubCategoryRepository repo;
    private final CategoryRepository categoryRepo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SubCategory>>> list(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "200") int limit) {
        Page<SubCategory> pg;
        if (categoryId != null) {
            pg = search.isBlank()
                    ? repo.findByDeletedAtIsNullAndCategory_Id(categoryId, pageRequest(page, limit))
                    : repo.findByDeletedAtIsNullAndCategory_IdAndNameContainingIgnoreCase(categoryId, search, pageRequest(page, limit));
        } else {
            pg = search.isBlank()
                    ? repo.findByDeletedAtIsNull(pageRequest(page, limit))
                    : repo.findByDeletedAtIsNullAndNameContainingIgnoreCase(search, pageRequest(page, limit));
        }
        return ResponseEntity.ok(ApiResponse.ok(pg.getContent(), buildMeta(pg, page, limit)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubCategory>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(findOrThrow(repo, id, "SubCategory")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SubCategory>> create(@RequestBody Map<String, Object> fields) {
        SubCategory e = new SubCategory();
        applyPatch(e, fields);
        if (fields.containsKey("categoryId")) {
            UUID catId = UUID.fromString(fields.get("categoryId").toString());
            Category cat = findOrThrow(categoryRepo, catId, "Category");
            e.setCategory(cat);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(repo.save(e)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SubCategory>> update(@PathVariable UUID id, @RequestBody Map<String, Object> fields) {
        SubCategory e = findOrThrow(repo, id, "SubCategory");
        applyPatch(e, fields);
        if (fields.containsKey("categoryId")) {
            UUID catId = UUID.fromString(fields.get("categoryId").toString());
            Category cat = findOrThrow(categoryRepo, catId, "Category");
            e.setCategory(cat);
        }
        return ResponseEntity.ok(ApiResponse.ok(repo.save(e)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        SubCategory e = findOrThrow(repo, id, "SubCategory");
        e.softDelete();
        repo.save(e);
        return ResponseEntity.ok(ApiResponse.deleted());
    }
}
