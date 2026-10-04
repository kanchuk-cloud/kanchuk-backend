package in.kanchuk.controller.admin;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.Coupon;
import in.kanchuk.repository.CouponRepository;
import in.kanchuk.service.GenericAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/coupons")
@RequiredArgsConstructor
public class AdminCouponController extends GenericAdminService {

    private final CouponRepository repo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Coupon>>> list(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        Page<Coupon> pg = search.isBlank()
                ? repo.findAll(pageRequest(page, limit))
                : repo.findByCodeContainingIgnoreCase(search, pageRequest(page, limit));
        return ResponseEntity.ok(ApiResponse.ok(pg.getContent(), buildMeta(pg, page, limit)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Coupon>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(findOrThrow(repo, id, "Coupon")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Coupon>> create(@RequestBody Map<String, Object> fields) {
        Coupon e = new Coupon();
        applyFields(e, fields);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(repo.save(e)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Coupon>> update(@PathVariable UUID id, @RequestBody Map<String, Object> fields) {
        Coupon e = findOrThrow(repo, id, "Coupon");
        applyFields(e, fields);
        return ResponseEntity.ok(ApiResponse.ok(repo.save(e)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        repo.deleteById(id);
        return ResponseEntity.ok(ApiResponse.deleted());
    }

    private void applyFields(Coupon e, Map<String, Object> f) {
        if (f.containsKey("code"))
            e.setCode(f.get("code").toString().toUpperCase().trim());
        if (f.containsKey("description")) {
            Object d = f.get("description");
            e.setDescription(d != null && !d.toString().isBlank() ? d.toString().trim() : null);
        }
        if (f.containsKey("type"))
            e.setType(Coupon.CouponType.valueOf(f.get("type").toString()));
        if (f.containsKey("discount"))
            e.setDiscount(new BigDecimal(f.get("discount").toString()));
        if (f.containsKey("minOrder"))
            e.setMinOrder(new BigDecimal(f.getOrDefault("minOrder", "0").toString()));
        if (f.containsKey("maxDiscount")) {
            Object md = f.get("maxDiscount");
            e.setMaxDiscount(md != null && !md.toString().isBlank() ? new BigDecimal(md.toString()) : null);
        }
        if (f.containsKey("expiryDate"))
            e.setExpiryDate(LocalDate.parse(f.get("expiryDate").toString()));
        if (f.containsKey("usageLimit")) {
            Object ul = f.get("usageLimit");
            e.setUsageLimit(ul != null && !ul.toString().isBlank() ? Integer.parseInt(ul.toString()) : null);
        }
        if (f.containsKey("isActive"))
            e.setActive(Boolean.parseBoolean(f.get("isActive").toString()));
    }
}
