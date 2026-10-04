package in.kanchuk.controller.admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.*;
import in.kanchuk.repository.*;
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
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController extends GenericAdminService {

    private final OrderRepository repo;
    private final OrderItemRepository itemRepo;
    private final PackagingRepository packagingRepo;
    private final DispatchingRepository dispatchingRepo;
    private final FulfillmentRepository fulfillmentRepo;
    private final ReturnRepository returnRepo;
    private final OrderItemRepository orderItemRepo;
    private final InventoryLevelRepository inventoryRepo;
    private final ObjectMapper objectMapper;
    private final SmsService smsService;
    private final WalletLedgerRepository walletLedgerRepo;
    private final UserRepository userRepo;
    private final LoyaltySettingsRepository loyaltySettingsRepo;

    // ── List ──────────────────────────────────────────────────────────────────

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> list(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        Page<Order> pg = search.isBlank()
                ? repo.findAll(pageRequest(page, limit))
                : repo.searchOrders(search, pageRequest(page, limit));
        List<Map<String, Object>> rows = pg.getContent().stream().map(this::buildListRow).toList();
        return ResponseEntity.ok(ApiResponse.ok(rows, buildMeta(pg, page, limit)));
    }

    // ── Detail ────────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<Map<String, Object>>> get(@PathVariable UUID id) {
        Order order = findOrThrow(repo, id, "Order");
        return ResponseEntity.ok(ApiResponse.ok(buildOrderMap(order)));
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<Order>> update(@PathVariable UUID id, @RequestBody Map<String, Object> fields) {
        Order e = findOrThrow(repo, id, "Order");
        String prevStatus = e.getStatus();
        applyPatch(e, fields);
        Order saved = repo.save(e);
        if ("cancelled".equals(saved.getStatus()) && !"cancelled".equals(prevStatus)) {
            reverseCancelledOrder(saved);
            smsService.sendOrderNotification(
                    orderPhone(saved), saved.getOrderNumber(), OrderNotificationType.ORDER_CANCELLED);
        }
        return ResponseEntity.ok(ApiResponse.ok(saved));
    }

    // ── Advance status ────────────────────────────────────────────────────────

    @PatchMapping("/{id}/advance")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> advance(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, Object> body) {
        if (body == null) body = Map.of();
        Order order = findOrThrow(repo, id, "Order");

        OrderNotificationType smsType = null;

        switch (order.getStatus()) {
            case "placed" -> {
                order.setStatus("confirmed");
                smsType = OrderNotificationType.ORDER_CONFIRMED;
            }

            case "confirmed" -> {
                Packaging pkg = new Packaging();
                pkg.setOrder(order);
                pkg.setStatus("packed");
                pkg.setPackedAt(OffsetDateTime.now());
                if (body.containsKey("packedBy"))     pkg.setPackedBy(body.get("packedBy").toString());
                if (body.containsKey("packageType"))  pkg.setPackageType(body.get("packageType").toString());
                if (body.containsKey("weightGrams") && body.get("weightGrams") != null)
                    pkg.setWeightGrams(Integer.parseInt(body.get("weightGrams").toString()));
                if (body.containsKey("notes"))        pkg.setNotes(body.get("notes").toString());
                packagingRepo.save(pkg);
                order.setStatus("packed");
                smsType = OrderNotificationType.ORDER_PACKED;
            }

            case "packed" -> {
                Fulfillment ful = new Fulfillment();
                ful.setOrder(order);
                ful.setStatus("shipped");
                ful.setShippedAt(OffsetDateTime.now());
                if (body.containsKey("trackingNumber")) ful.setTrackingNumber(body.get("trackingNumber").toString());
                if (body.containsKey("carrier"))        ful.setCarrier(body.get("carrier").toString());
                if (body.containsKey("notes"))          ful.setNotes(body.get("notes").toString());
                ful = fulfillmentRepo.save(ful);

                Dispatching dis = new Dispatching();
                dis.setOrder(order);
                dis.setFulfillment(ful);
                packagingRepo.findByOrderId(id).ifPresent(dis::setPackaging);
                dis.setStatus("dispatched");
                dis.setDispatchedAt(OffsetDateTime.now());
                if (body.containsKey("carrier"))       dis.setCarrier(body.get("carrier").toString());
                if (body.containsKey("awbNumber"))     dis.setAwbNumber(body.get("awbNumber").toString());
                if (body.containsKey("dispatchType"))  dis.setDispatchType(body.get("dispatchType").toString());
                if (body.containsKey("dispatchedBy"))  dis.setDispatchedBy(body.get("dispatchedBy").toString());
                if (body.containsKey("notes"))         dis.setNotes(body.get("notes").toString());
                dispatchingRepo.save(dis);
                order.setStatus("shipped");
                smsType = OrderNotificationType.ORDER_SHIPPED;
            }

            case "shipped" -> {
                fulfillmentRepo.findByOrderId(id).stream().findFirst().ifPresent(f -> {
                    f.setStatus("out_for_delivery");
                    fulfillmentRepo.save(f);
                });
                order.setStatus("out_for_delivery");
                smsType = OrderNotificationType.OUT_FOR_DELIVERY;
            }

            case "out_for_delivery" -> {
                fulfillmentRepo.findByOrderId(id).stream().findFirst().ifPresent(f -> {
                    f.setStatus("delivered");
                    f.setDeliveredAt(OffsetDateTime.now());
                    fulfillmentRepo.save(f);
                });
                order.setStatus("delivered");
                smsType = OrderNotificationType.ORDER_DELIVERED;

                // Activate any pending coin earns for this order
                walletLedgerRepo.findByOrder_IdAndType(id, "COIN_EARN").forEach(earn -> {
                    if (earn.isPending() && earn.getCoinAmount() != null && earn.getCoinAmount() > 0) {
                        User u = earn.getUser();
                        u.setLoyaltyPoints(u.getLoyaltyPoints() + earn.getCoinAmount());
                        earn.setCoinBalanceAfter(u.getLoyaltyPoints());
                        earn.setPending(false);
                        userRepo.save(u);
                        walletLedgerRepo.save(earn);
                    }
                });
            }

            default -> {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Cannot advance order with status: " + order.getStatus()));
            }
        }

        repo.save(order);
        if (smsType != null) {
            smsService.sendOrderNotification(orderPhone(order), order.getOrderNumber(), smsType);
        }
        return ResponseEntity.ok(ApiResponse.ok(buildOrderMap(order)));
    }

    // ── Cancel reversal ───────────────────────────────────────────────────────

    private void reverseCancelledOrder(Order order) {
        UUID orderId = order.getId();
        UUID userId = order.getUser() != null ? order.getUser().getId() : null;

        if (userId != null) {
            User user = userRepo.findById(userId).orElse(null);
            if (user != null) {
                // Reverse COIN_REDEEM
                walletLedgerRepo.findByOrder_IdAndType(orderId, "COIN_REDEEM").forEach(entry -> {
                    int coins = Math.abs(entry.getCoinAmount() != null ? entry.getCoinAmount() : 0);
                    if (coins > 0) {
                        user.setLoyaltyPoints(user.getLoyaltyPoints() + coins);
                        WalletLedger rev = new WalletLedger();
                        rev.setUser(user);
                        rev.setOrder(order);
                        rev.setType("COIN_REVERSE");
                        rev.setCoinAmount(coins);
                        rev.setCoinBalanceAfter(user.getLoyaltyPoints());
                        rev.setNote("Coins reversed for cancelled order " + order.getOrderNumber());
                        walletLedgerRepo.save(rev);
                    }
                });

                // Reverse WALLET_DEBIT
                walletLedgerRepo.findByOrder_IdAndType(orderId, "WALLET_DEBIT").forEach(entry -> {
                    BigDecimal refund = entry.getAmount() != null ? entry.getAmount().abs() : BigDecimal.ZERO;
                    if (refund.compareTo(BigDecimal.ZERO) > 0) {
                        user.setWalletBalance(user.getWalletBalance().add(refund));
                        WalletLedger rev = new WalletLedger();
                        rev.setUser(user);
                        rev.setOrder(order);
                        rev.setType("WALLET_REVERSE");
                        rev.setAmount(refund);
                        rev.setBalanceAfter(user.getWalletBalance());
                        rev.setNote("Wallet refunded for cancelled order " + order.getOrderNumber());
                        walletLedgerRepo.save(rev);
                    }
                });

                // Cancel pending COIN_EARN
                walletLedgerRepo.findByOrder_IdAndType(orderId, "COIN_EARN").forEach(earn -> {
                    if (earn.isPending()) {
                        earn.setNote((earn.getNote() != null ? earn.getNote() : "") + " [CANCELLED]");
                        earn.setPending(false);
                        earn.setCoinAmount(0);
                        walletLedgerRepo.save(earn);
                    }
                });

                userRepo.save(user);
            }
        }

        // Restore inventory for each order item
        itemRepo.findByOrderId(orderId).forEach(item -> {
            try {
                Map<String, Object> snap = objectMapper.readValue(
                        item.getProductSnapshot(), new com.fasterxml.jackson.core.type.TypeReference<>() {});
                String sku = snap.getOrDefault("sku", "").toString();
                if (!sku.isBlank()) {
                    List<InventoryLevel> levels = inventoryRepo.findByVariantSku(sku);
                    if (!levels.isEmpty()) {
                        InventoryLevel level = levels.get(0);
                        level.setQuantityOnHand(level.getQuantityOnHand() + item.getQuantity());
                        inventoryRepo.save(level);
                    }
                }
            } catch (Exception ignored) {}
        });
    }

    // ── Initiate return ───────────────────────────────────────────────────────

    @PostMapping("/{id}/return")
    @Transactional
    public ResponseEntity<ApiResponse<Return>> initiateReturn(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> body) {
        Order order = findOrThrow(repo, id, "Order");
        Return r = new Return();
        r.setOrder(order);
        r.setStatus("requested");
        r.setRequestedAt(OffsetDateTime.now());
        if (body.containsKey("reason") && body.get("reason") != null)
            r.setReason(body.get("reason").toString());
        if (body.containsKey("refundAmount") && body.get("refundAmount") != null)
            r.setRefundAmount(new BigDecimal(body.get("refundAmount").toString()));
        if (body.containsKey("orderItemId") && body.get("orderItemId") != null) {
            orderItemRepo.findById(UUID.fromString(body.get("orderItemId").toString()))
                    .ifPresent(r::setOrderItem);
        }
        Return saved = returnRepo.save(r);
        smsService.sendOrderNotification(
                orderPhone(order), order.getOrderNumber(), OrderNotificationType.RETURN_INITIATED);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(saved));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Resolves customer phone from user entity first, then address snapshot. */
    private String orderPhone(Order o) {
        if (o.getUser() != null && o.getUser().getPhone() != null) return o.getUser().getPhone();
        return o.getAddressSnapshot().getOrDefault("phone", null);
    }

    // ── Builders ──────────────────────────────────────────────────────────────

    private Map<String, Object> buildListRow(Order o) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", o.getId());
        m.put("orderNumber", o.getOrderNumber());
        m.put("status", o.getStatus());
        m.put("total", o.getTotal());
        m.put("paymentMethod", o.getPaymentMethod());
        m.put("paymentStatus", o.getPaymentStatus());
        m.put("addressSnapshot", o.getAddressSnapshot());
        m.put("placedAt", o.getPlacedAt());
        m.put("updatedAt", o.getUpdatedAt());
        if (o.getUser() != null) {
            m.put("customerName", o.getUser().getName());
            m.put("customerEmail", o.getUser().getEmail());
            m.put("customerPhone", o.getUser().getPhone());
            m.put("userId", o.getUser().getId());
        } else {
            Map<String, String> snap = o.getAddressSnapshot();
            m.put("customerName", snap.getOrDefault("name", null));
            m.put("customerPhone", snap.getOrDefault("phone", null));
        }
        List<Return> rets = returnRepo.findByOrderId(o.getId());
        if (!rets.isEmpty()) {
            Return latest = rets.get(rets.size() - 1);
            m.put("returns", List.of(Map.of(
                "id", latest.getId(),
                "status", latest.getStatus(),
                "requestedAt", latest.getRequestedAt() != null ? latest.getRequestedAt().toString() : ""
            )));
        } else {
            m.put("returns", List.of());
        }
        return m;
    }

    private Map<String, Object> buildOrderMap(Order o) {
        UUID id = o.getId();
        Map<String, Object> m = buildListRow(o);
        m.put("subtotal", o.getSubtotal());
        m.put("discountAmount", o.getDiscountAmount());
        m.put("deliveryCharge", o.getDeliveryCharge());

        // amount stores the rupee value (negative debit); fall back to coinAmount / coinsPerRupee
        // for legacy entries created before the amount field was stored
        List<WalletLedger> redeems = walletLedgerRepo.findByOrder_IdAndType(id, "COIN_REDEEM");
        BigDecimal coinDiscount;
        if (redeems.isEmpty()) {
            coinDiscount = BigDecimal.ZERO;
        } else {
            boolean hasRupeeAmount = redeems.stream()
                    .anyMatch(wl -> wl.getAmount() != null && wl.getAmount().compareTo(BigDecimal.ZERO) != 0);
            if (hasRupeeAmount) {
                coinDiscount = redeems.stream()
                        .map(wl -> wl.getAmount() != null ? wl.getAmount().abs() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            } else {
                BigDecimal coinsPerRupee = loyaltySettingsRepo.findById(1L)
                        .map(LoyaltySettings::getCoinsPerRupee)
                        .orElse(BigDecimal.ONE);
                coinDiscount = redeems.stream()
                        .filter(wl -> wl.getCoinAmount() != null && wl.getCoinAmount() != 0)
                        .map(wl -> BigDecimal.valueOf(Math.abs(wl.getCoinAmount()))
                                .divide(coinsPerRupee, 2, java.math.RoundingMode.DOWN))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
        }
        m.put("coinDiscount", coinDiscount);

        // Tax summary
        m.put("subtotalTaxable", o.getSubtotalTaxable());
        m.put("totalCgst",       o.getTotalCgst());
        m.put("totalSgst",       o.getTotalSgst());
        m.put("totalIgst",       o.getTotalIgst());
        m.put("totalTax",        o.getTotalTax());

        // Items
        m.put("items", itemRepo.findByOrderId(id).stream().map(item -> {
            Map<String, Object> im = new LinkedHashMap<>();
            im.put("id", item.getId());
            im.put("price", item.getPrice());
            im.put("quantity", item.getQuantity());
            im.put("taxableValue",  item.getTaxableValue());
            im.put("gstRate",       item.getGstRate());
            im.put("taxAmount",     item.getTaxAmount());
            im.put("cgst",          item.getCgst());
            im.put("sgst",          item.getSgst());
            im.put("igst",          item.getIgst());
            im.put("isInterState",  item.isInterState());
            try {
                im.put("productSnapshot", objectMapper.readValue(
                        item.getProductSnapshot(), new TypeReference<Map<String, Object>>() {}));
            } catch (Exception e) {
                im.put("productSnapshot", Map.of());
            }
            return im;
        }).toList());

        // Sub-records
        m.put("packaging", packagingRepo.findByOrderId(id).orElse(null));
        m.put("dispatching", dispatchingRepo.findByOrderId(id).orElse(null));
        m.put("fulfillment", fulfillmentRepo.findByOrderId(id).stream().findFirst().orElse(null));
        m.put("returns", returnRepo.findByOrderId(id));

        return m;
    }
}
