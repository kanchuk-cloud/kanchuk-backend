package in.kanchuk.controller.pub;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.Product;
import in.kanchuk.repository.ProductRepository;
import in.kanchuk.service.GenericAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/public/products")
@RequiredArgsConstructor
public class PublicProductController extends GenericAdminService {

    private final ProductRepository repo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> list(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        Page<Product> pg;
        if (!category.isBlank()) {
            pg = repo.findByCategorySlugAndDeletedAtIsNullAndIsActiveTrue(category, pageRequest(page, limit));
        } else if (!search.isBlank()) {
            pg = repo.searchProducts(search, pageRequest(page, limit));
        } else {
            pg = repo.findByDeletedAtIsNullAndIsActiveTrue(pageRequest(page, limit));
        }
        return ResponseEntity.ok(ApiResponse.ok(pg.getContent(), buildMeta(pg, page, limit)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(findOrThrow(repo, id, "Product")));
    }
}
