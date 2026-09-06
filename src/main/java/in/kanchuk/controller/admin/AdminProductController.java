package in.kanchuk.controller.admin;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.*;
import in.kanchuk.repository.*;
import in.kanchuk.service.GenericAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
public class AdminProductController extends GenericAdminService {

    private final ProductRepository repo;
    private final ProductImageRepository imageRepo;
    private final ProductVariantRepository variantRepo;
    private final ProductListingRepository listingRepo;
    private final ProductOptionTypeRepository optionTypeRepo;
    private final ProductOptionValueRepository optionValueRepo;
    private final VariantOptionValueRepository variantOptionValueRepo;
    private final CategoryRepository categoryRepo;
    private final SubCategoryRepository subCategoryRepo;
    private final FabricRepository fabricRepo;
    private final DesignerRepository designerRepo;
    private final TaxCategoryRepository taxCategoryRepo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> list(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        Page<Product> pg;
        if (categoryId != null) {
            pg = search.isBlank()
                    ? repo.findByDeletedAtIsNullAndCategory_Id(categoryId, pageRequest(page, limit))
                    : repo.searchProductsByCategory(categoryId, search, pageRequest(page, limit));
        } else {
            pg = search.isBlank()
                    ? repo.findByDeletedAtIsNull(pageRequest(page, limit))
                    : repo.searchProducts(search, pageRequest(page, limit));
        }
        return ResponseEntity.ok(ApiResponse.ok(pg.getContent(), buildMeta(pg, page, limit)));
    }

    @GetMapping("/next-sku")
    public ResponseEntity<ApiResponse<String>> nextSku(@RequestParam String prefix) {
        String likePrefix = prefix + "-";
        List<String> skus = repo.findSkusByPrefix(likePrefix);
        int maxNum = skus.stream()
                .map(sku -> sku.substring(likePrefix.length()))
                .filter(suffix -> suffix.matches("\\d+"))
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(0);
        return ResponseEntity.ok(ApiResponse.ok(prefix + "-" + String.format("%04d", maxNum + 1)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> get(@PathVariable UUID id) {
        Product p = repo.findWithRelationsById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Product not found: " + id));
        return ResponseEntity.ok(ApiResponse.ok(p));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Product>> create(@RequestBody Map<String, Object> fields) {
        Product e = new Product();
        applyPatch(e, fields);
        applyProductRelations(e, fields);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(repo.save(e)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> update(@PathVariable UUID id, @RequestBody Map<String, Object> fields) {
        Product e = findOrThrow(repo, id, "Product");
        applyPatch(e, fields);
        applyProductRelations(e, fields);
        return ResponseEntity.ok(ApiResponse.ok(repo.save(e)));
    }

    private void applyProductRelations(Product e, Map<String, Object> fields) {
        if (fields.containsKey("categoryId") && fields.get("categoryId") != null) {
            categoryRepo.findById(UUID.fromString(fields.get("categoryId").toString()))
                    .ifPresent(e::setCategory);
        }
        if (fields.containsKey("subCategoryId") && fields.get("subCategoryId") != null) {
            subCategoryRepo.findById(UUID.fromString(fields.get("subCategoryId").toString()))
                    .ifPresent(e::setSubCategory);
        }
        if (fields.containsKey("fabricId") && fields.get("fabricId") != null) {
            fabricRepo.findById(UUID.fromString(fields.get("fabricId").toString()))
                    .ifPresent(e::setFabric);
        }
        if (fields.containsKey("designerId") && fields.get("designerId") != null) {
            designerRepo.findById(UUID.fromString(fields.get("designerId").toString()))
                    .ifPresent(e::setDesigner);
        }
        if (fields.containsKey("taxCategoryId") && fields.get("taxCategoryId") != null) {
            taxCategoryRepo.findById(UUID.fromString(fields.get("taxCategoryId").toString()))
                    .ifPresent(e::setTaxCategory);
        }
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<ApiResponse<Product>> setActive(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> body) {
        Product e = findOrThrow(repo, id, "Product");
        if (body.containsKey("isActive")) {
            e.setActive(Boolean.parseBoolean(body.get("isActive").toString()));
        } else {
            e.setActive(!e.isActive());
        }
        return ResponseEntity.ok(ApiResponse.ok(repo.save(e)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        Product e = findOrThrow(repo, id, "Product");
        e.softDelete();
        repo.save(e);
        return ResponseEntity.ok(ApiResponse.deleted());
    }

    @GetMapping("/{id}/images")
    public ResponseEntity<ApiResponse<List<ProductImage>>> listImages(@PathVariable UUID id) {
        findOrThrow(repo, id, "Product");
        return ResponseEntity.ok(ApiResponse.ok(imageRepo.findByProductIdOrderBySortOrderAsc(id)));
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<ApiResponse<ProductImage>> addImage(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> body) {
        Product product = findOrThrow(repo, id, "Product");
        ProductImage img = new ProductImage();
        img.setProduct(product);
        img.setImageUrl(body.getOrDefault("url", "").toString());
        if (body.containsKey("imageType"))  img.setImageType(body.get("imageType").toString());
        if (body.containsKey("sortOrder"))  img.setSortOrder(Integer.parseInt(body.get("sortOrder").toString()));
        if (body.containsKey("variantId") && body.get("variantId") != null)
            img.setVariantId(java.util.UUID.fromString(body.get("variantId").toString()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(imageRepo.save(img)));
    }

    @DeleteMapping("/{id}/images/{imageId}")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @PathVariable UUID id,
            @PathVariable UUID imageId) {
        findOrThrow(repo, id, "Product");
        imageRepo.deleteById(imageId);
        return ResponseEntity.ok(ApiResponse.deleted());
    }

    // ── Variants & Pricing ──────────────────────────────────────────────────────

    @GetMapping("/{id}/variants")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listVariants(@PathVariable UUID id) {
        findOrThrow(repo, id, "Product");
        List<Map<String, Object>> result = variantRepo
                .findByProductIdAndDeletedAtIsNullOrderByCreatedAtAsc(id)
                .stream()
                .map(v -> buildVariantMap(v))
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @PostMapping("/{id}/variants")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> createVariant(
            @PathVariable UUID id, @RequestBody Map<String, Object> body) {
        Product product = findOrThrow(repo, id, "Product");
        String sku = body.getOrDefault("sku", "").toString();

        // Guard against duplicate SKU (e.g. re-creating a previously soft-deleted variant)
        if (variantRepo.findBySkuAndDeletedAtIsNull(sku).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Variant with SKU '" + sku + "' already exists"));
        }

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setSku(sku);
        if (body.containsKey("isActive")) {
            variant.setActive(Boolean.parseBoolean(body.get("isActive").toString()));
        }
        variant = variantRepo.save(variant);

        ProductListing listing = new ProductListing();
        listing.setVariant(variant);
        listing.setPrice(new BigDecimal(body.getOrDefault("price", "0").toString()));
        if (body.get("compareAtPrice") != null) {
            listing.setCompareAtPrice(new BigDecimal(body.get("compareAtPrice").toString()));
        }
        listingRepo.save(listing);

        // Link option values (color, size, etc.) sent as optionValueIds array
        if (body.containsKey("optionValueIds") && body.get("optionValueIds") instanceof List<?> ids) {
            final ProductVariant savedVariant = variant;
            for (Object rawId : ids) {
                UUID ovId = UUID.fromString(rawId.toString());
                optionValueRepo.findById(ovId).ifPresent(ov -> {
                    VariantOptionValue vov = new VariantOptionValue();
                    VariantOptionValue.VariantOptionValueId vovId = new VariantOptionValue.VariantOptionValueId();
                    vovId.setVariantId(savedVariant.getId());
                    vovId.setOptionValueId(ovId);
                    vov.setId(vovId);
                    vov.setVariant(savedVariant);
                    vov.setOptionValue(ov);
                    variantOptionValueRepo.save(vov);
                });
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(buildVariantMap(variant)));
    }

    @PutMapping("/{id}/variants/{variantId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateVariant(
            @PathVariable UUID id, @PathVariable UUID variantId, @RequestBody Map<String, Object> body) {
        findOrThrow(repo, id, "Product");
        ProductVariant variant = variantRepo.findById(variantId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Variant not found: " + variantId));
        if (body.containsKey("sku")) variant.setSku(body.get("sku").toString());
        if (body.containsKey("isActive")) variant.setActive(Boolean.parseBoolean(body.get("isActive").toString()));
        variantRepo.save(variant);

        ProductListing listing = listingRepo.findFirstByVariantIdAndDeletedAtIsNull(variantId)
                .orElseGet(() -> { ProductListing l = new ProductListing(); l.setVariant(variant); return l; });
        if (body.containsKey("price") && body.get("price") != null) {
            listing.setPrice(new BigDecimal(body.get("price").toString()));
        }
        if (body.containsKey("compareAtPrice")) {
            listing.setCompareAtPrice(body.get("compareAtPrice") == null ? null :
                    new BigDecimal(body.get("compareAtPrice").toString()));
        }
        listing = listingRepo.save(listing);

        return ResponseEntity.ok(ApiResponse.ok(buildVariantMap(variant)));
    }

    @DeleteMapping("/{id}/variants/{variantId}")
    public ResponseEntity<ApiResponse<Void>> deleteVariant(
            @PathVariable UUID id, @PathVariable UUID variantId) {
        findOrThrow(repo, id, "Product");
        ProductVariant variant = variantRepo.findById(variantId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Variant not found: " + variantId));
        variant.softDelete();
        variantRepo.save(variant);
        return ResponseEntity.ok(ApiResponse.deleted());
    }

    private Map<String, Object> buildVariantMap(ProductVariant v) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", v.getId());
        m.put("sku", v.getSku());
        m.put("isActive", v.isActive());
        listingRepo.findFirstByVariantIdAndDeletedAtIsNull(v.getId()).ifPresent(l -> {
            m.put("listingId", l.getId());
            m.put("price", l.getPrice());
            m.put("compareAtPrice", l.getCompareAtPrice());
        });
        List<Map<String, Object>> optVals = variantOptionValueRepo
                .findByVariantIdWithDetails(v.getId())
                .stream().map(vov -> {
                    Map<String, Object> ov = new LinkedHashMap<>();
                    ov.put("id", vov.getOptionValue().getId());
                    ov.put("optionTypeName", vov.getOptionValue().getOptionType().getName());
                    ov.put("value", vov.getOptionValue().getValue());
                    ov.put("code", vov.getOptionValue().getCode());
                    return ov;
                }).toList();
        m.put("optionValues", optVals);
        return m;
    }

    // ── Options (Colors / Sizes) ────────────────────────────────────────────────

    @GetMapping("/{id}/options")
    public ResponseEntity<ApiResponse<List<ProductOptionType>>> listOptions(@PathVariable UUID id) {
        findOrThrow(repo, id, "Product");
        return ResponseEntity.ok(ApiResponse.ok(optionTypeRepo.findByProductIdOrderBySortOrderAsc(id)));
    }

    @PostMapping("/{id}/options/values")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> addOptionValue(
            @PathVariable UUID id, @RequestBody Map<String, Object> body) {
        Product product = findOrThrow(repo, id, "Product");
        String optionTypeName        = body.get("optionTypeName").toString();
        String optionTypeDisplayName = body.getOrDefault("optionTypeDisplayName", optionTypeName).toString();
        String value                 = body.get("value").toString();
        String code                  = body.get("code").toString().toUpperCase();

        // Find or create option type
        ProductOptionType optionType = optionTypeRepo.findByProductIdAndName(id, optionTypeName)
                .orElseGet(() -> {
                    ProductOptionType t = new ProductOptionType();
                    t.setProduct(product);
                    t.setName(optionTypeName);
                    t.setDisplayName(optionTypeDisplayName);
                    return optionTypeRepo.save(t);
                });

        // Create option value
        ProductOptionValue optionValue = new ProductOptionValue();
        optionValue.setOptionType(optionType);
        optionValue.setValue(value);
        optionValue.setCode(code);
        optionValue.setSortOrder((int) optionValueRepo.countByOptionTypeId(optionType.getId()));
        optionValue = optionValueRepo.save(optionValue);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("optionValueId", optionValue.getId());
        result.put("value", value);
        result.put("code", code);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(result));
    }

    @DeleteMapping("/{id}/options/values/{valueId}")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> deleteOptionValue(
            @PathVariable UUID id, @PathVariable UUID valueId) {
        findOrThrow(repo, id, "Product");
        variantOptionValueRepo.deleteByOptionValueId(valueId);
        optionValueRepo.deleteById(valueId);
        return ResponseEntity.ok(ApiResponse.deleted());
    }

    @DeleteMapping("/{id}/options/{typeId}")
    public ResponseEntity<ApiResponse<Void>> deleteOptionType(
            @PathVariable UUID id, @PathVariable UUID typeId) {
        findOrThrow(repo, id, "Product");
        optionTypeRepo.deleteById(typeId);
        return ResponseEntity.ok(ApiResponse.deleted());
    }
}
