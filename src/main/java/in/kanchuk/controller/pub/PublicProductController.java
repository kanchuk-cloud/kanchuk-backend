package in.kanchuk.controller.pub;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.Product;
import in.kanchuk.entity.ProductOptionType;
import in.kanchuk.repository.ProductListingRepository;
import in.kanchuk.repository.ProductOptionTypeRepository;
import in.kanchuk.repository.ProductRepository;
import in.kanchuk.repository.ProductVariantRepository;
import in.kanchuk.repository.VariantOptionValueRepository;
import in.kanchuk.service.GenericAdminService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/public/products")
@RequiredArgsConstructor
public class PublicProductController extends GenericAdminService {

    private final ProductRepository repo;
    private final ProductVariantRepository variantRepo;
    private final ProductListingRepository listingRepo;
    private final ProductOptionTypeRepository optionTypeRepo;
    private final VariantOptionValueRepository variantOptionValueRepo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> list(
            @RequestParam(defaultValue = "")    String  search,
            @RequestParam(defaultValue = "")    String  category,
            @RequestParam(defaultValue = "")    String  gender,
            @RequestParam(defaultValue = "false") boolean isNew,
            @RequestParam(defaultValue = "1")   int     page,
            @RequestParam(defaultValue = "20")  int     limit) {

        Page<Product> pg;

        if (!category.isBlank() && !gender.isBlank()) {
            pg = repo.findByGenderAndCategorySlugAndDeletedAtIsNullAndIsActiveTrue(gender, category, pageRequest(page, limit));
        } else if (!category.isBlank()) {
            pg = repo.findByCategorySlugAndDeletedAtIsNullAndIsActiveTrue(category, pageRequest(page, limit));
        } else if (!gender.isBlank() && isNew) {
            pg = repo.findByGenderAndIsNewTrueAndDeletedAtIsNullAndIsActiveTrue(gender, pageRequest(page, limit));
        } else if (!gender.isBlank()) {
            pg = repo.findByGenderAndDeletedAtIsNullAndIsActiveTrue(gender, pageRequest(page, limit));
        } else if (isNew) {
            pg = repo.findByIsNewTrueAndDeletedAtIsNullAndIsActiveTrue(pageRequest(page, limit));
        } else if (!search.isBlank()) {
            pg = repo.searchProducts(search, pageRequest(page, limit));
        } else {
            pg = repo.findByDeletedAtIsNullAndIsActiveTrue(pageRequest(page, limit));
        }

        return ResponseEntity.ok(ApiResponse.ok(pg.getContent(), buildMeta(pg, page, limit)));
    }

    @GetMapping("/{sku}")
    public ResponseEntity<ApiResponse<Product>> getBySku(@PathVariable String sku) {
        Product product = repo.findBySkuAndDeletedAtIsNull(sku)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + sku));
        return ResponseEntity.ok(ApiResponse.ok(product));
    }

    @GetMapping("/by-id/{id}")
    public ResponseEntity<ApiResponse<Product>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(findOrThrow(repo, id, "Product")));
    }

    @GetMapping("/{sku}/variants")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getVariantsBySku(@PathVariable String sku) {
        Product product = repo.findBySkuAndDeletedAtIsNull(sku)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + sku));
        List<Map<String, Object>> result = variantRepo
                .findByProductIdAndDeletedAtIsNullOrderByCreatedAtAsc(product.getId())
                .stream()
                .filter(in.kanchuk.entity.ProductVariant::isActive)
                .map(v -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", v.getId());
                    m.put("sku", v.getSku());
                    listingRepo.findFirstByVariantIdAndDeletedAtIsNull(v.getId()).ifPresent(l -> {
                        m.put("price", l.getPrice());
                        m.put("compareAtPrice", l.getCompareAtPrice());
                    });
                    List<Map<String, Object>> optionValues = variantOptionValueRepo
                            .findByVariantIdWithDetails(v.getId())
                            .stream().map(vov -> {
                                Map<String, Object> ov = new LinkedHashMap<>();
                                ov.put("id", vov.getOptionValue().getId());
                                ov.put("optionTypeName", vov.getOptionValue().getOptionType().getName());
                                ov.put("value", vov.getOptionValue().getValue());
                                ov.put("code", vov.getOptionValue().getCode());
                                return ov;
                            }).toList();
                    m.put("optionValues", optionValues);
                    return m;
                })
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{sku}/options")
    public ResponseEntity<ApiResponse<List<ProductOptionType>>> getOptionsBySku(@PathVariable String sku) {
        List<ProductOptionType> options = optionTypeRepo.findByProductSkuOrderBySortOrderAsc(sku);
        return ResponseEntity.ok(ApiResponse.ok(options));
    }
}
