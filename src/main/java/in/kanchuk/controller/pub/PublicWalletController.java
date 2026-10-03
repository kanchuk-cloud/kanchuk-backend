package in.kanchuk.controller.pub;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.LoyaltySettings;
import in.kanchuk.entity.Order;
import in.kanchuk.entity.User;
import in.kanchuk.entity.WalletLedger;
import in.kanchuk.repository.*;
import in.kanchuk.service.GenericAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/v1/public/wallet")
@RequiredArgsConstructor
public class PublicWalletController extends GenericAdminService {

    private final UserRepository userRepo;
    private final OrderRepository orderRepo;
    private final LoyaltySettingsRepository settingsRepo;
    private final WalletLedgerRepository ledgerRepo;

    private LoyaltySettings settings() {
        return settingsRepo.findById(1L).orElseGet(() -> {
            LoyaltySettings s = new LoyaltySettings();
            s.setId(1L);
            return settingsRepo.save(s);
        });
    }

    // GET /public/wallet/me?userId=...
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getWallet(@RequestParam UUID userId) {
        User u = findOrThrow(userRepo, userId, "User");
        LoyaltySettings s = settings();

        // Coins expiring within 30 days
        LocalDate thirtyDaysOut = LocalDate.now().plusDays(30);
        Integer expiring = ledgerRepo.sumActiveCoinEarns(userId, LocalDate.now());
        // Rough expiry warning: check ledger for COIN_EARN within window
        int expiringCoins = (expiring != null) ? expiring : 0;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("balance", u.getWalletBalance());
        result.put("coinBalance", u.getLoyaltyPoints());
        result.put("coinWorth", BigDecimal.valueOf(u.getLoyaltyPoints()).divide(s.getCoinsPerRupee(), 2, RoundingMode.DOWN));
        result.put("expiringCoins", 0);
        result.put("expiringDate", thirtyDaysOut);
        result.put("ledger", ledgerRepo.findRecentByUserId(userId, PageRequest.of(0, 10)));
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // POST /public/wallet/apply — validate and calculate discounts (does NOT deduct yet)
    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<Map<String, Object>>> apply(@RequestBody Map<String, Object> body) {
        UUID userId = UUID.fromString(body.get("userId").toString());
        boolean useWallet = Boolean.parseBoolean(body.getOrDefault("useWallet", "false").toString());
        int coinsToRedeem = Integer.parseInt(body.getOrDefault("useCoins", "0").toString());
        BigDecimal orderSubtotal = new BigDecimal(body.getOrDefault("orderSubtotal", "0").toString());

        User u = findOrThrow(userRepo, userId, "User");
        LoyaltySettings s = settings();

        // Validate coins
        if (coinsToRedeem < 0) coinsToRedeem = 0;
        if (coinsToRedeem > u.getLoyaltyPoints()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Insufficient coin balance"));
        }

        // Max coins redeemable = orderSubtotal * redeemMaxPct% * coinsPerRupee
        BigDecimal maxCoinValue = orderSubtotal
                .multiply(s.getCoinsRedeemMaxPct())
                .divide(new BigDecimal("100"), 2, RoundingMode.DOWN);
        BigDecimal maxCoinsRedeemable = maxCoinValue.multiply(s.getCoinsPerRupee()).setScale(0, RoundingMode.DOWN);
        if (BigDecimal.valueOf(coinsToRedeem).compareTo(maxCoinsRedeemable) > 0) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Max redeemable coins for this order: " + maxCoinsRedeemable.intValue()));
        }

        // Coin discount in rupees
        BigDecimal coinDiscount = BigDecimal.valueOf(coinsToRedeem).divide(s.getCoinsPerRupee(), 2, RoundingMode.DOWN);

        // Wallet discount
        BigDecimal walletDiscount = BigDecimal.ZERO;
        if (useWallet) {
            BigDecimal afterCoins = orderSubtotal.subtract(coinDiscount);
            walletDiscount = u.getWalletBalance().min(afterCoins.max(BigDecimal.ZERO));
        }

        BigDecimal newTotal = orderSubtotal.subtract(coinDiscount).subtract(walletDiscount).max(BigDecimal.ZERO);
        boolean willEarnCoins = coinsToRedeem == 0 || s.isCoinsOnCoinPayment();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("coinDiscount", coinDiscount);
        result.put("walletDiscount", walletDiscount);
        result.put("newTotal", newTotal);
        result.put("willEarnCoins", willEarnCoins);
        result.put("maxCoinsRedeemable", maxCoinsRedeemable.intValue());
        result.put("maxCoinValue", maxCoinValue);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    // POST /public/wallet/finalize-order — called after order confirmed to deduct and schedule earn
    @PostMapping("/finalize-order")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> finalizeOrder(@RequestBody Map<String, Object> body) {
        UUID userId = UUID.fromString(body.get("userId").toString());
        UUID orderId = UUID.fromString(body.get("orderId").toString());
        int coinsRedeemed = Integer.parseInt(body.getOrDefault("coinsRedeemed", "0").toString());
        BigDecimal walletDebited = new BigDecimal(body.getOrDefault("walletDebited", "0").toString());
        BigDecimal orderTotal = new BigDecimal(body.getOrDefault("orderTotal", "0").toString());

        User u = findOrThrow(userRepo, userId, "User");
        Order order = findOrThrow(orderRepo, orderId, "Order");
        LoyaltySettings s = settings();

        // Deduct coins
        if (coinsRedeemed > 0) {
            int newCoins = Math.max(0, u.getLoyaltyPoints() - coinsRedeemed);
            u.setLoyaltyPoints(newCoins);
            BigDecimal coinRupeeValue = BigDecimal.valueOf(coinsRedeemed)
                    .divide(s.getCoinsPerRupee(), 2, RoundingMode.DOWN);
            WalletLedger coinEntry = new WalletLedger();
            coinEntry.setUser(u);
            coinEntry.setOrder(order);
            coinEntry.setType("COIN_REDEEM");
            coinEntry.setAmount(coinRupeeValue.negate());
            coinEntry.setCoinAmount(-coinsRedeemed);
            coinEntry.setCoinBalanceAfter(newCoins);
            ledgerRepo.save(coinEntry);
        }

        // Deduct wallet
        if (walletDebited.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal newBal = u.getWalletBalance().subtract(walletDebited).max(BigDecimal.ZERO);
            u.setWalletBalance(newBal);
            WalletLedger walletEntry = new WalletLedger();
            walletEntry.setUser(u);
            walletEntry.setOrder(order);
            walletEntry.setType("WALLET_DEBIT");
            walletEntry.setAmount(walletDebited.negate());
            walletEntry.setBalanceAfter(newBal);
            ledgerRepo.save(walletEntry);
        }

        // Schedule COIN_EARN (pending until return window closes)
        boolean willEarnCoins = (coinsRedeemed == 0 || s.isCoinsOnCoinPayment())
                && orderTotal.compareTo(s.getMinOrderForCoins()) >= 0;
        if (willEarnCoins) {
            int coinsEarned = orderTotal
                    .multiply(s.getCoinsEarnPct())
                    .divide(new BigDecimal("100"), 0, RoundingMode.DOWN)
                    .multiply(s.getCoinsPerRupee())
                    .intValue();
            if (coinsEarned > 0) {
                WalletLedger earnEntry = new WalletLedger();
                earnEntry.setUser(u);
                earnEntry.setOrder(order);
                earnEntry.setType("COIN_EARN");
                earnEntry.setCoinAmount(coinsEarned);
                earnEntry.setExpiresAt(s.getCoinsExpiryDays() > 0
                        ? LocalDate.now().plusDays(s.getCoinsExpiryDays()) : null);
                earnEntry.setPending(true);
                earnEntry.setNote("Earned on order " + order.getOrderNumber() + " — pending return window");
                ledgerRepo.save(earnEntry);
            }
        }

        userRepo.save(u);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // POST /public/wallet/reverse-order — called on cancellation or return
    @PostMapping("/reverse-order")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> reverseOrder(@RequestBody Map<String, Object> body) {
        UUID userId = UUID.fromString(body.get("userId").toString());
        UUID orderId = UUID.fromString(body.get("orderId").toString());

        User u = findOrThrow(userRepo, userId, "User");
        Order order = findOrThrow(orderRepo, orderId, "Order");

        // Reverse COIN_REDEEM
        List<WalletLedger> redeems = ledgerRepo.findByOrder_IdAndType(orderId, "COIN_REDEEM");
        for (WalletLedger entry : redeems) {
            int refundCoins = Math.abs(entry.getCoinAmount() != null ? entry.getCoinAmount() : 0);
            if (refundCoins > 0) {
                u.setLoyaltyPoints(u.getLoyaltyPoints() + refundCoins);
                WalletLedger rev = new WalletLedger();
                rev.setUser(u);
                rev.setOrder(order);
                rev.setType("COIN_REVERSE");
                rev.setCoinAmount(refundCoins);
                rev.setCoinBalanceAfter(u.getLoyaltyPoints());
                rev.setNote("Reversed coin redemption for order " + order.getOrderNumber());
                ledgerRepo.save(rev);
            }
        }

        // Reverse WALLET_DEBIT
        List<WalletLedger> debits = ledgerRepo.findByOrder_IdAndType(orderId, "WALLET_DEBIT");
        for (WalletLedger entry : debits) {
            BigDecimal refund = entry.getAmount().abs();
            if (refund.compareTo(BigDecimal.ZERO) > 0) {
                u.setWalletBalance(u.getWalletBalance().add(refund));
                WalletLedger rev = new WalletLedger();
                rev.setUser(u);
                rev.setOrder(order);
                rev.setType("WALLET_REVERSE");
                rev.setAmount(refund);
                rev.setBalanceAfter(u.getWalletBalance());
                rev.setNote("Refunded to wallet for order " + order.getOrderNumber());
                ledgerRepo.save(rev);
            }
        }

        // Cancel pending COIN_EARN
        List<WalletLedger> pendingEarns = ledgerRepo.findByOrder_IdAndType(orderId, "COIN_EARN");
        for (WalletLedger earn : pendingEarns) {
            if (earn.isPending()) {
                earn.setNote((earn.getNote() != null ? earn.getNote() : "") + " [CANCELLED]");
                earn.setPending(false);
                earn.setCoinAmount(0);
                ledgerRepo.save(earn);
            }
        }

        userRepo.save(u);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // POST /public/wallet/activate-coins — called after return window closes to credit earned coins
    @PostMapping("/activate-coins")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> activateCoins(@RequestBody Map<String, Object> body) {
        UUID orderId = UUID.fromString(body.get("orderId").toString());
        List<WalletLedger> pending = ledgerRepo.findByOrder_IdAndType(orderId, "COIN_EARN");
        for (WalletLedger earn : pending) {
            if (earn.isPending() && earn.getCoinAmount() != null && earn.getCoinAmount() > 0) {
                User u = earn.getUser();
                u.setLoyaltyPoints(u.getLoyaltyPoints() + earn.getCoinAmount());
                earn.setCoinBalanceAfter(u.getLoyaltyPoints());
                earn.setPending(false);
                userRepo.save(u);
                ledgerRepo.save(earn);
            }
        }
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
