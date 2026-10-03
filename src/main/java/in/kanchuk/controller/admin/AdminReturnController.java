package in.kanchuk.controller.admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.InventoryLevel;
import in.kanchuk.entity.OrderItem;
import in.kanchuk.entity.Return;
import in.kanchuk.entity.User;
import in.kanchuk.entity.WalletLedger;
import in.kanchuk.repository.InventoryLevelRepository;
import in.kanchuk.repository.OrderItemRepository;
import in.kanchuk.repository.OrderRepository;
import in.kanchuk.repository.ReturnRepository;
import in.kanchuk.repository.UserRepository;
import in.kanchuk.repository.WalletLedgerRepository;
import in.kanchuk.service.GenericAdminService;
import in.kanchuk.service.SmsService;
import in.kanchuk.sms.template.OrderNotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/admin/returns")
@RequiredArgsConstructor
public class AdminReturnController extends GenericAdminService {

    private final ReturnRepository repo;
    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final InventoryLevelRepository inventoryRepo;
    private final UserRepository userRepo;
    private final WalletLedgerRepository walletLedgerRepo;
    private final ObjectMapper objectMapper;
    private final SmsService smsService;

    // ── List ──────────────────────────────────────────────────────────────────

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        Page<Return> pg = repo.findAll(pageRequest(page, limit));
        List<Map<String, Object>> rows = pg.getContent().stream().map(this::buildRow).toList();
        return ResponseEntity.ok(ApiResponse.ok(rows, buildMeta(pg, page, limit)));
    }

    // ── Detail ────────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<Map<String, Object>>> get(@PathVariable UUID id) {
        Return r = findOrThrow(repo, id, "Return");
        return ResponseEntity.ok(ApiResponse.ok(buildRow(r)));
    }

    // ── Create (admin-initiated on behalf of customer) ────────────────────────

    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> body) {
        Return e = new Return();
        if (body.containsKey("orderId") && body.get("orderId") != null) {
            orderRepo.findById(UUID.fromString(body.get("orderId").toString()))
                    .ifPresent(e::setOrder);
        }
        if (body.containsKey("orderItemId") && body.get("orderItemId") != null) {
            orderItemRepo.findById(UUID.fromString(body.get("orderItemId").toString()))
                    .ifPresent(e::setOrderItem);
        }
        e.setStatus("requested");
        OffsetDateTime now = OffsetDateTime.now();
        e.setRequestedAt(now);
        e.setExpectedReturnBy(now.plusDays(12));
        if (body.containsKey("reason") && body.get("reason") != null)
            e.setReason(body.get("reason").toString());
        Return saved = repo.save(e);
        if (saved.getOrder() != null) {
            String phone = returnOrderPhone(saved);
            String orderNumber = saved.getOrder().getOrderNumber();
            smsService.sendOrderNotification(phone, orderNumber, OrderNotificationType.RETURN_INITIATED);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(buildRow(saved)));
    }

    // ── Advance ───────────────────────────────────────────────────────────────

    @PatchMapping("/{id}/advance")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> advance(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, Object> body) {
        if (body == null) body = Map.of();
        Return e = findOrThrow(repo, id, "Return");
        String action = body.containsKey("action") ? body.get("action").toString() : "";
        String orderNumber = e.getOrder() != null ? e.getOrder().getOrderNumber() : "";
        String phone = returnOrderPhone(e);
        OrderNotificationType smsType = null;

        switch (e.getStatus()) {

            case "requested" -> {
                if ("reject".equals(action)) {
                    e.setStatus("rejected");
                    if (body.containsKey("rejectionReason") && body.get("rejectionReason") != null)
                        e.setRejectionReason(body.get("rejectionReason").toString());
                    smsType = OrderNotificationType.RETURN_REJECTED;
                } else {
                    e.setStatus("approved");
                    smsType = OrderNotificationType.RETURN_APPROVED;
                }
            }

            case "approved" -> {
                e.setStatus("pickup_scheduled");
                e.setPickupScheduledAt(OffsetDateTime.now());
                if (body.containsKey("awbNumber") && body.get("awbNumber") != null)
                    e.setAwbNumber(body.get("awbNumber").toString());
                if (body.containsKey("carrier") && body.get("carrier") != null)
                    e.setCarrier(body.get("carrier").toString());
            }

            case "pickup_scheduled" -> {
                e.setStatus("received");
                e.setReceivedAt(OffsetDateTime.now());
                if (body.containsKey("inspectionNotes") && body.get("inspectionNotes") != null)
                    e.setInspectionNotes(body.get("inspectionNotes").toString());
            }

            case "received" -> {
                if ("reject".equals(action)) {
                    e.setStatus("rejected");
                    if (body.containsKey("rejectionReason") && body.get("rejectionReason") != null)
                        e.setRejectionReason(body.get("rejectionReason").toString());
                    smsType = OrderNotificationType.RETURN_REJECTED;
                } else {
                    e.setStatus("resolved");
                    e.setResolvedAt(OffsetDateTime.now());
                    restoreStock(e);
                    processReturnRefund(e);
                }
            }

            case "resolved" -> {
                e.setStatus("refunded");
                smsType = OrderNotificationType.REFUND_COMPLETED;
            }

            default -> {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Cannot advance return with status: " + e.getStatus()));
            }
        }

        repo.save(e);
        if (smsType != null) smsService.sendOrderNotification(phone, orderNumber, smsType);
        return ResponseEntity.ok(ApiResponse.ok(buildRow(e)));
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        Return e = findOrThrow(repo, id, "Return");
        repo.delete(e);
        return ResponseEntity.ok(ApiResponse.deleted());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void processReturnRefund(Return ret) {
        if (ret.getOrder() == null) return;
        UUID orderId = ret.getOrder().getId();

        // Resolve the real User entity (lazy proxy may not be fully loaded)
        UUID userId = ret.getOrder().getUser() != null ? ret.getOrder().getUser().getId() : null;
        if (userId == null) return;
        User user = userRepo.findById(userId).orElse(null);
        if (user == null) return;

        // 1. Restore coins that were redeemed on this order
        List<WalletLedger> coinRedeems = walletLedgerRepo.findByOrder_IdAndType(orderId, "COIN_REDEEM");
        int totalCoinsRestored = 0;
        for (WalletLedger entry : coinRedeems) {
            int coins = Math.abs(entry.getCoinAmount() != null ? entry.getCoinAmount() : 0);
            if (coins > 0) {
                user.setLoyaltyPoints(user.getLoyaltyPoints() + coins);
                totalCoinsRestored += coins;
                WalletLedger rev = new WalletLedger();
                rev.setUser(user);
                rev.setOrder(ret.getOrder());
                rev.setType("COIN_REVERSE");
                rev.setCoinAmount(coins);
                rev.setCoinBalanceAfter(user.getLoyaltyPoints());
                rev.setNote("Coins restored for return of order " + ret.getOrder().getOrderNumber());
                walletLedgerRepo.save(rev);
            }
        }

        // 2. Reverse any wallet debit back to wallet
        List<WalletLedger> walletDebits = walletLedgerRepo.findByOrder_IdAndType(orderId, "WALLET_DEBIT");
        for (WalletLedger entry : walletDebits) {
            BigDecimal refund = entry.getAmount() != null ? entry.getAmount().abs() : BigDecimal.ZERO;
            if (refund.compareTo(BigDecimal.ZERO) > 0) {
                user.setWalletBalance(user.getWalletBalance().add(refund));
                WalletLedger rev = new WalletLedger();
                rev.setUser(user);
                rev.setOrder(ret.getOrder());
                rev.setType("WALLET_REVERSE");
                rev.setAmount(refund);
                rev.setBalanceAfter(user.getWalletBalance());
                rev.setNote("Wallet debit reversed for return of order " + ret.getOrder().getOrderNumber());
                walletLedgerRepo.save(rev);
            }
        }

        // 3. Cancel any pending coin earnings on this order
        List<WalletLedger> pendingEarns = walletLedgerRepo.findByOrder_IdAndType(orderId, "COIN_EARN");
        for (WalletLedger earn : pendingEarns) {
            if (earn.isPending()) {
                earn.setNote((earn.getNote() != null ? earn.getNote() : "") + " [CANCELLED - RETURNED]");
                earn.setPending(false);
                earn.setCoinAmount(0);
                walletLedgerRepo.save(earn);
            }
        }

        // 4. Credit the cash amount paid (order.total) to Kanchuk Wallet
        BigDecimal cashRefund = ret.getOrder().getTotal() != null ? ret.getOrder().getTotal() : BigDecimal.ZERO;
        if (cashRefund.compareTo(BigDecimal.ZERO) > 0) {
            user.setWalletBalance(user.getWalletBalance().add(cashRefund));
            WalletLedger credit = new WalletLedger();
            credit.setUser(user);
            credit.setOrder(ret.getOrder());
            credit.setType("WALLET_CREDIT");
            credit.setAmount(cashRefund);
            credit.setBalanceAfter(user.getWalletBalance());
            credit.setNote("Return refund credited for order " + ret.getOrder().getOrderNumber());
            walletLedgerRepo.save(credit);
        }

        userRepo.save(user);

        // Set refund amounts on the Return record
        ret.setRefundAmount(cashRefund);
        if (totalCoinsRestored > 0) ret.setCoinsRefunded(totalCoinsRestored);
    }

    private void restoreStock(Return ret) {
        if (ret.getOrder() == null) return;
        List<OrderItem> items = orderItemRepo.findByOrderId(ret.getOrder().getId());
        for (OrderItem item : items) {
            String sku = extractSku(item.getProductSnapshot());
            if (sku == null || sku.isBlank()) continue;
            List<InventoryLevel> levels = inventoryRepo.findByVariantSku(sku);
            if (levels.isEmpty()) continue;
            InventoryLevel level = levels.get(0);
            level.setQuantityOnHand(level.getQuantityOnHand() + item.getQuantity());
            inventoryRepo.save(level);
        }
    }

    private String extractSku(String snapshotJson) {
        if (snapshotJson == null) return null;
        try {
            Map<String, Object> snap = objectMapper.readValue(snapshotJson, new TypeReference<>() {});
            Object sku = snap.get("sku");
            return sku != null ? sku.toString() : null;
        } catch (Exception ignored) { return null; }
    }

    private String returnOrderPhone(Return r) {
        var order = r.getOrder();
        if (order == null) return null;
        if (order.getUser() != null && order.getUser().getPhone() != null) return order.getUser().getPhone();
        return order.getAddressSnapshot() != null ? order.getAddressSnapshot().getOrDefault("phone", null) : null;
    }

    private Map<String, Object> buildRow(Return r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("status", r.getStatus());
        m.put("reason", r.getReason());
        m.put("refundAmount", r.getRefundAmount());
        m.put("coinsRefunded", r.getCoinsRefunded());
        m.put("requestedAt", r.getRequestedAt());
        m.put("resolvedAt", r.getResolvedAt());
        m.put("expectedReturnBy", r.getExpectedReturnBy());
        m.put("rejectionReason", r.getRejectionReason());
        m.put("awbNumber", r.getAwbNumber());
        m.put("carrier", r.getCarrier());
        m.put("pickupScheduledAt", r.getPickupScheduledAt());
        m.put("receivedAt", r.getReceivedAt());
        m.put("inspectionNotes", r.getInspectionNotes());
        m.put("createdAt", r.getCreatedAt());
        m.put("updatedAt", r.getUpdatedAt());
        if (r.getOrder() != null) {
            m.put("orderId", r.getOrder().getId());
            m.put("orderNumber", r.getOrder().getOrderNumber());
            m.put("orderTotal", r.getOrder().getTotal());
        }
        if (r.getOrderItem() != null) {
            m.put("orderItemId", r.getOrderItem().getId());
        }
        return m;
    }
}
