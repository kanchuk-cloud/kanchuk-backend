package in.kanchuk.controller.pub;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.Coupon;
import in.kanchuk.repository.CouponRepository;
import in.kanchuk.service.GenericAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/public/coupons")
@RequiredArgsConstructor
public class PublicCouponController extends GenericAdminService {

    private final CouponRepository repo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Coupon>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(repo.findActiveAndNotExpired(LocalDate.now())));
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validate(@RequestBody Map<String, Object> body) {
        String code = body.getOrDefault("code", "").toString().trim();
        BigDecimal orderSubtotal = new BigDecimal(body.getOrDefault("orderSubtotal", "0").toString());

        Optional<Coupon> opt = repo.findByCodeIgnoreCase(code);
        if (opt.isEmpty())
            return ResponseEntity.status(404).body(ApiResponse.error("Invalid coupon code"));

        Coupon c = opt.get();

        if (!c.isActive())
            return ResponseEntity.badRequest().body(ApiResponse.error("Coupon is inactive"));

        if (c.getExpiryDate().isBefore(LocalDate.now()))
            return ResponseEntity.badRequest().body(ApiResponse.error("Coupon has expired"));

        if (c.getUsageLimit() != null && c.getUsageCount() >= c.getUsageLimit())
            return ResponseEntity.badRequest().body(ApiResponse.error("Coupon usage limit reached"));

        if (orderSubtotal.compareTo(c.getMinOrder()) < 0)
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Minimum order of ₹" + c.getMinOrder().setScale(0, RoundingMode.HALF_UP) + " required"));

        BigDecimal discountAmount;
        if (c.getType() == Coupon.CouponType.FLAT) {
            discountAmount = c.getDiscount();
        } else {
            discountAmount = orderSubtotal.multiply(c.getDiscount())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (c.getMaxDiscount() != null && discountAmount.compareTo(c.getMaxDiscount()) > 0)
                discountAmount = c.getMaxDiscount();
        }

        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "code", c.getCode(),
                "description", c.getDescription() != null ? c.getDescription() : "",
                "type", c.getType().name(),
                "discountAmount", discountAmount,
                "minOrder", c.getMinOrder(),
                "expiryDate", c.getExpiryDate().toString()
        )));
    }
}
