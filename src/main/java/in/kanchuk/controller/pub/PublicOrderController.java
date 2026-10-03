package in.kanchuk.controller.pub;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.Order;
import in.kanchuk.entity.OrderItem;
import in.kanchuk.entity.Return;
import in.kanchuk.entity.User;
import in.kanchuk.entity.InventoryLevel;
import in.kanchuk.repository.InventoryLevelRepository;
import in.kanchuk.repository.OrderItemRepository;
import in.kanchuk.repository.OrderRepository;
import in.kanchuk.repository.ReturnRepository;
import in.kanchuk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/public/orders")
@RequiredArgsConstructor
public class PublicOrderController {

    private final OrderRepository orderRepo;
    private final OrderItemRepository itemRepo;
    private final UserRepository userRepo;
    private final ReturnRepository returnRepo;
    private final InventoryLevelRepository inventoryRepo;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @GetMapping("/{orderNumber}")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<Map<String, Object>>> getByOrderNumber(@PathVariable String orderNumber) {
        return orderRepo.findByOrderNumber(orderNumber)
                .map(o -> ResponseEntity.ok(ApiResponse.ok(buildOrderSummary(o))))
                .orElse(ResponseEntity.status(404).body(ApiResponse.error("Order not found")));
    }

    @GetMapping("/saved-addresses")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> savedAddresses(
            @RequestParam String phone) {
        Optional<User> userOpt = userRepo.findByPhone(phone.trim());
        if (userOpt.isEmpty()) return ResponseEntity.ok(ApiResponse.ok(List.of()));

        Set<String> seen = new LinkedHashSet<>();
        List<Map<String, String>> addresses = new ArrayList<>();
        for (Order o : orderRepo.findByUserIdOrderByPlacedAtDesc(userOpt.get().getId())) {
            Map<String, String> snap = o.getAddressSnapshot();
            if (snap.isEmpty() || !snap.containsKey("line1")) continue;
            String key = snap.getOrDefault("line1", "") + "|" + snap.getOrDefault("pincode", "");
            if (seen.add(key)) addresses.add(snap);
        }
        return ResponseEntity.ok(ApiResponse.ok(addresses));
    }

    @GetMapping("/lookup")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> lookupByPhone(
            @RequestParam String phone) {
        Optional<User> userOpt = userRepo.findByPhone(phone.trim());
        if (userOpt.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.ok(List.of()));
        }
        List<Map<String, Object>> orders = orderRepo.findByUserIdOrderByPlacedAtDesc(userOpt.get().getId())
                .stream()
                .map(o -> buildOrderSummary(o))
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(orders));
    }

    private Map<String, Object> buildOrderSummary(Order o) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", o.getId());
        m.put("orderNumber", o.getOrderNumber());
        m.put("status", o.getStatus());
        m.put("total", o.getTotal());
        m.put("subtotal", o.getSubtotal());
        m.put("discountAmount", o.getDiscountAmount());
        m.put("deliveryCharge", o.getDeliveryCharge());
        m.put("paymentMethod", o.getPaymentMethod());
        m.put("paymentStatus", o.getPaymentStatus());
        m.put("placedAt", o.getPlacedAt());
        m.put("addressSnapshot", o.getAddressSnapshot());
        m.put("items", itemRepo.findByOrderId(o.getId()).stream().map(item -> {
            Map<String, Object> im = new LinkedHashMap<>();
            im.put("id", item.getId());
            im.put("price", item.getPrice());
            im.put("quantity", item.getQuantity());
            try {
                im.put("productSnapshot", objectMapper.readValue(
                        item.getProductSnapshot(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}));
            } catch (Exception e) {
                im.put("productSnapshot", Map.of());
            }
            return im;
        }).toList());
        // Return info for status display on list page
        List<Return> returns = returnRepo.findByOrderId(o.getId());
        if (!returns.isEmpty()) {
            Return ret = returns.get(0);
            m.put("returnStatus", ret.getStatus());
            m.put("returnExpectedBy", ret.getExpectedReturnBy());
            m.put("returnRefundAmount", ret.getRefundAmount());
            m.put("returnCoinsRefunded", ret.getCoinsRefunded());
            m.put("returnResolvedAt", ret.getResolvedAt() != null ? ret.getResolvedAt() : ret.getUpdatedAt());
        }
        return m;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> placeOrder(@RequestBody Map<String, Object> body) {
        // Generate order number: KCH-{YEAR}-{NNNNNN}
        String year = String.valueOf(LocalDate.now().getYear());
        String prefix = "KCH-" + year + "-";
        List<String> existing = orderRepo.findOrderNumbersByPrefix(prefix);
        int maxNum = existing.stream()
                .map(n -> n.substring(prefix.length()))
                .filter(s -> s.matches("\\d+"))
                .mapToInt(Integer::parseInt)
                .max().orElse(0);
        String orderNumber = prefix + String.format("%06d", maxNum + 1);

        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setStatus("placed");
        order.setPlacedAt(OffsetDateTime.now());
        order.setSubtotal(new BigDecimal(body.getOrDefault("subtotal", "0").toString()));
        order.setDiscountAmount(new BigDecimal(body.getOrDefault("discountAmount", "0").toString()));
        order.setDeliveryCharge(new BigDecimal(body.getOrDefault("deliveryCharge", "0").toString()));
        order.setTotal(new BigDecimal(body.getOrDefault("total", "0").toString()));
        order.setPaymentMethod(body.getOrDefault("paymentMethod", "").toString());
        order.setPaymentStatus("pending");

        // Address snapshot
        @SuppressWarnings("unchecked")
        Map<String, Object> address = body.containsKey("address") ? (Map<String, Object>) body.get("address") : Map.of();
        if (!address.isEmpty()) {
            try { order.setDeliveryAddressSnapshot(objectMapper.writeValueAsString(address)); }
            catch (Exception ignored) {}
        }

        // Resolve user: prefer explicit userId, fall back to phone lookup/creation
        User user = resolveUser(body, address);
        if (user != null) order.setUser(user);

        order = orderRepo.save(order);

        // Order items + inventory deduction
        if (body.containsKey("items") && body.get("items") instanceof List<?> rawItems) {
            final Order saved = order;
            for (Object rawItem : rawItems) {
                if (rawItem instanceof Map<?, ?> rawMap) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> item = (Map<String, Object>) rawMap;
                    OrderItem oi = new OrderItem();
                    oi.setOrder(saved);
                    oi.setPrice(new BigDecimal(item.getOrDefault("price", "0").toString()));
                    int qty = Integer.parseInt(item.getOrDefault("quantity", "1").toString());
                    oi.setQuantity(qty);
                    try { oi.setProductSnapshot(objectMapper.writeValueAsString(item)); }
                    catch (Exception ignored) {}
                    itemRepo.save(oi);

                    // Deduct from inventory: pick the location with highest on-hand first
                    String sku = item.getOrDefault("sku", "").toString();
                    if (!sku.isBlank()) {
                        int remaining = qty;
                        List<InventoryLevel> levels = inventoryRepo.findByVariantSku(sku);
                        for (InventoryLevel level : levels) {
                            if (remaining <= 0) break;
                            int deduct = Math.min(remaining, level.getQuantityOnHand());
                            level.setQuantityOnHand(level.getQuantityOnHand() - deduct);
                            inventoryRepo.save(level);
                            remaining -= deduct;
                        }
                    }
                }
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", order.getId());
        result.put("orderNumber", order.getOrderNumber());
        result.put("status", order.getStatus());
        result.put("total", order.getTotal());
        result.put("placedAt", order.getPlacedAt());
        if (user != null) {
            result.put("userId", user.getId());
            result.put("userName", user.getName());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(result));
    }

    private User resolveUser(Map<String, Object> body, Map<String, Object> address) {
        // Logged-in user: client sends userId
        if (body.containsKey("userId") && body.get("userId") != null) {
            try {
                return userRepo.findById(UUID.fromString(body.get("userId").toString())).orElse(null);
            } catch (Exception ignored) {}
        }

        // Guest: find or create by phone
        String phone = address.getOrDefault("phone", "").toString().trim();
        if (phone.isEmpty()) return null;

        return userRepo.findByPhone(phone).orElseGet(() -> {
            String name = address.getOrDefault("name", "Guest").toString();
            User guest = new User();
            guest.setName(name);
            guest.setPhone(phone);
            guest.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
            guest.setActive(true);
            return userRepo.save(guest);
        });
    }
}
