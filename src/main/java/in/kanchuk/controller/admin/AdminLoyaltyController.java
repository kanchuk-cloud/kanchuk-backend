package in.kanchuk.controller.admin;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.LoyaltySettings;
import in.kanchuk.entity.User;
import in.kanchuk.entity.WalletLedger;
import in.kanchuk.repository.LoyaltySettingsRepository;
import in.kanchuk.repository.UserRepository;
import in.kanchuk.repository.WalletLedgerRepository;
import in.kanchuk.service.GenericAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

@RestController
@RequiredArgsConstructor
public class AdminLoyaltyController extends GenericAdminService {

    private final LoyaltySettingsRepository settingsRepo;
    private final UserRepository userRepo;
    private final WalletLedgerRepository ledgerRepo;

    // ── Settings ──────────────────────────────────────────────────────────────

    @GetMapping("/api/v1/admin/loyalty-settings")
    public ResponseEntity<ApiResponse<LoyaltySettings>> getSettings() {
        LoyaltySettings s = settingsRepo.findById(1L).orElseGet(() -> {
            LoyaltySettings defaults = new LoyaltySettings();
            defaults.setId(1L);
            defaults.setCreatedAt(OffsetDateTime.now());
            return settingsRepo.save(defaults);
        });
        return ResponseEntity.ok(ApiResponse.ok(s));
    }

    @PutMapping("/api/v1/admin/loyalty-settings")
    @Transactional
    public ResponseEntity<ApiResponse<LoyaltySettings>> updateSettings(@RequestBody Map<String, Object> fields) {
        LoyaltySettings s = settingsRepo.findById(1L).orElseGet(() -> {
            LoyaltySettings defaults = new LoyaltySettings();
            defaults.setId(1L);
            defaults.setCreatedAt(OffsetDateTime.now());
            return settingsRepo.save(defaults);
        });
        applyPatch(s, fields);
        // Guard: percentages 0-100, days >= 0, coinsPerRupee > 0
        if (s.getCoinsEarnPct().compareTo(BigDecimal.ZERO) < 0) s.setCoinsEarnPct(BigDecimal.ZERO);
        if (s.getCoinsEarnPct().compareTo(new BigDecimal("100")) > 0) s.setCoinsEarnPct(new BigDecimal("100"));
        if (s.getCoinsRedeemMaxPct().compareTo(BigDecimal.ZERO) < 0) s.setCoinsRedeemMaxPct(BigDecimal.ZERO);
        if (s.getCoinsRedeemMaxPct().compareTo(new BigDecimal("100")) > 0) s.setCoinsRedeemMaxPct(new BigDecimal("100"));
        if (s.getCoinsPerRupee().compareTo(BigDecimal.ONE) < 0) s.setCoinsPerRupee(BigDecimal.ONE);
        if (s.getCoinsExpiryDays() < 0) s.setCoinsExpiryDays(0);
        if (s.getWalletExpiryDays() < 0) s.setWalletExpiryDays(0);
        if (s.getMinOrderForCoins().compareTo(BigDecimal.ZERO) < 0) s.setMinOrderForCoins(BigDecimal.ZERO);
        return ResponseEntity.ok(ApiResponse.ok(settingsRepo.save(s)));
    }

    // ── Wallets list ──────────────────────────────────────────────────────────

    @GetMapping("/api/v1/admin/wallets")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listWallets(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {

        Page<User> users = search.isBlank()
                ? userRepo.findByDeletedAtIsNull(pageRequest(page, limit))
                : userRepo.searchCustomers(search, pageRequest(page, limit));

        List<Map<String, Object>> result = new ArrayList<>();
        for (User u : users.getContent()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("userId", u.getId());
            row.put("userName", u.getName());
            row.put("userEmail", u.getEmail());
            row.put("userPhone", u.getPhone());
            row.put("balance", u.getWalletBalance());
            row.put("coinBalance", u.getLoyaltyPoints());
            row.put("createdAt", u.getCreatedAt());
            result.add(row);
        }
        return ResponseEntity.ok(ApiResponse.ok(result, buildMeta(users, page, limit)));
    }

    // ── Single wallet + ledger ────────────────────────────────────────────────

    @GetMapping("/api/v1/admin/wallets/{userId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getWallet(@PathVariable UUID userId) {
        User u = findOrThrow(userRepo, userId, "User");
        List<WalletLedger> ledger = ledgerRepo.findRecentByUserId(userId, PageRequest.of(0, 50));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userId", u.getId());
        data.put("userName", u.getName());
        data.put("userEmail", u.getEmail());
        data.put("balance", u.getWalletBalance());
        data.put("coinBalance", u.getLoyaltyPoints());
        data.put("ledger", ledger);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    // ── Admin adjust ──────────────────────────────────────────────────────────

    @PostMapping("/api/v1/admin/wallets/{userId}/adjust")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> adjust(
            @PathVariable UUID userId,
            @RequestBody Map<String, Object> body) {

        String type = body.getOrDefault("type", "").toString();
        String note = body.getOrDefault("note", "").toString().trim();
        if (note.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Note is required for admin adjustments"));
        }

        BigDecimal amount = new BigDecimal(body.getOrDefault("amount", "0").toString());
        User u = findOrThrow(userRepo, userId, "User");
        WalletLedger entry = new WalletLedger();
        entry.setUser(u);
        entry.setNote(note);
        entry.setType(type);

        if ("WALLET_ADMIN_ADJUST".equals(type)) {
            BigDecimal newBal = u.getWalletBalance().add(amount);
            if (newBal.compareTo(BigDecimal.ZERO) < 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Wallet balance cannot go below zero"));
            }
            u.setWalletBalance(newBal);
            entry.setAmount(amount);
            entry.setBalanceAfter(newBal);
        } else if ("COIN_ADMIN_ADJUST".equals(type)) {
            int coins = amount.intValue();
            int newCoins = u.getLoyaltyPoints() + coins;
            if (newCoins < 0) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Coin balance cannot go below zero"));
            }
            u.setLoyaltyPoints(newCoins);
            entry.setCoinAmount(coins);
            entry.setCoinBalanceAfter(newCoins);
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid type. Use WALLET_ADMIN_ADJUST or COIN_ADMIN_ADJUST"));
        }

        userRepo.save(u);
        ledgerRepo.save(entry);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("balance", u.getWalletBalance());
        result.put("coinBalance", u.getLoyaltyPoints());
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
