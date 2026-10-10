package in.kanchuk.controller.pub;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.repository.LoyaltySettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/public/loyalty")
@RequiredArgsConstructor
public class PublicLoyaltyController {

    private final LoyaltySettingsRepository repo;

    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSettings() {
        return repo.findById(1L).map(s -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("platformFee",         s.getPlatformFee());
            m.put("coinsEarnPct",        s.getCoinsEarnPct());
            m.put("coinsRedeemMaxPct",   s.getCoinsRedeemMaxPct());
            m.put("coinsPerRupee",       s.getCoinsPerRupee());
            m.put("coinsOnCoinPayment",  s.isCoinsOnCoinPayment());
            m.put("platformFeeEnabled",  s.isPlatformFeeEnabled());
            return ResponseEntity.ok(ApiResponse.ok(m));
        }).orElse(ResponseEntity.status(404).body(ApiResponse.error("Settings not found")));
    }
}
