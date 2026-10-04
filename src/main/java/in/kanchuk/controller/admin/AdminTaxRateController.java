package in.kanchuk.controller.admin;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.TaxCategory;
import in.kanchuk.entity.TaxRate;
import in.kanchuk.repository.TaxCategoryRepository;
import in.kanchuk.repository.TaxRateRepository;
import in.kanchuk.service.GenericAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/tax-rates")
@RequiredArgsConstructor
public class AdminTaxRateController extends GenericAdminService {

    private final TaxRateRepository repo;
    private final TaxCategoryRepository taxCategoryRepo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TaxRate>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        Page<TaxRate> pg = repo.findAll(pageRequest(page, limit));
        return ResponseEntity.ok(ApiResponse.ok(pg.getContent(), buildMeta(pg, page, limit)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaxRate>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(findOrThrow(repo, id, "TaxRate")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TaxRate>> create(@RequestBody Map<String, Object> fields) {
        TaxRate e = new TaxRate();
        String catId = fields.getOrDefault("taxCategoryId", "").toString().trim();
        TaxCategory cat = findOrThrow(taxCategoryRepo, UUID.fromString(catId), "TaxCategory");
        e.setTaxCategory(cat);
        e.setRate(new BigDecimal(fields.getOrDefault("rate", "0").toString()));
        e.setTaxType(fields.getOrDefault("taxType", "GST").toString());
        Object sc = fields.get("stateCode");
        e.setStateCode(sc != null && !sc.toString().isBlank() ? sc.toString().toUpperCase() : null);
        Object active = fields.get("isActive");
        e.setActive(active == null || Boolean.parseBoolean(active.toString()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(repo.save(e)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaxRate>> update(@PathVariable UUID id, @RequestBody Map<String, Object> fields) {
        TaxRate e = findOrThrow(repo, id, "TaxRate");
        Object catId = fields.get("taxCategoryId");
        if (catId != null && !catId.toString().isBlank()) {
            TaxCategory cat = findOrThrow(taxCategoryRepo, UUID.fromString(catId.toString().trim()), "TaxCategory");
            e.setTaxCategory(cat);
        }
        applyPatch(e, fields);
        return ResponseEntity.ok(ApiResponse.ok(repo.save(e)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        repo.deleteById(id);
        return ResponseEntity.ok(ApiResponse.deleted());
    }
}
