package in.kanchuk.controller.pub;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.Order;
import in.kanchuk.entity.OrderItem;
import in.kanchuk.entity.User;
import in.kanchuk.repository.OrderItemRepository;
import in.kanchuk.repository.OrderRepository;
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
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

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

        // Order items
        if (body.containsKey("items") && body.get("items") instanceof List<?> rawItems) {
            final Order saved = order;
            for (Object rawItem : rawItems) {
                if (rawItem instanceof Map<?, ?> rawMap) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> item = (Map<String, Object>) rawMap;
                    OrderItem oi = new OrderItem();
                    oi.setOrder(saved);
                    oi.setPrice(new BigDecimal(item.getOrDefault("price", "0").toString()));
                    oi.setQuantity(Integer.parseInt(item.getOrDefault("quantity", "1").toString()));
                    try { oi.setProductSnapshot(objectMapper.writeValueAsString(item)); }
                    catch (Exception ignored) {}
                    itemRepo.save(oi);
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
            String email = "guest." + phone + "@kanchuk.in";
            // Deduplicate email if somehow already taken
            if (userRepo.findByEmail(email).isPresent()) {
                email = "guest." + phone + "." + System.currentTimeMillis() + "@kanchuk.in";
            }
            User guest = new User();
            guest.setName(name);
            guest.setEmail(email);
            guest.setPhone(phone);
            guest.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
            guest.setActive(true);
            return userRepo.save(guest);
        });
    }
}
