package in.kanchuk.controller.admin;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.Seller;
import in.kanchuk.repository.SellerRepository;
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
@RequestMapping("/api/v1/admin/sellers")
@RequiredArgsConstructor
public class AdminSellerController extends GenericAdminService {

    private final SellerRepository repo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Seller>>> list(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        Page<Seller> pg = search.isBlank()
                ? repo.findByDeletedAtIsNull(pageRequest(page, limit))
                : repo.findByDeletedAtIsNullAndNameContainingIgnoreCase(search, pageRequest(page, limit));
        return ResponseEntity.ok(ApiResponse.ok(pg.getContent(), buildMeta(pg, page, limit)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Seller>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(findOrThrow(repo, id, "Seller")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Seller>> create(@RequestBody Seller body) {
        body.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(repo.save(body)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Seller>> update(@PathVariable UUID id, @RequestBody Map<String, Object> fields) {
        Seller e = findOrThrow(repo, id, "Seller");
        applyPatch(e, fields);
        return ResponseEntity.ok(ApiResponse.ok(repo.save(e)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        Seller e = findOrThrow(repo, id, "Seller");
        e.softDelete();
        repo.save(e);
        return ResponseEntity.ok(ApiResponse.deleted());
    }
}
