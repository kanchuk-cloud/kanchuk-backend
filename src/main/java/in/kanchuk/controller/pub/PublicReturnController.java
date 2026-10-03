package in.kanchuk.controller.pub;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.Order;
import in.kanchuk.entity.Return;
import in.kanchuk.repository.OrderRepository;
import in.kanchuk.repository.ReturnRepository;
import in.kanchuk.service.SmsService;
import in.kanchuk.sms.template.OrderNotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/public/returns")
@RequiredArgsConstructor
public class PublicReturnController {

    private final OrderRepository orderRepo;
    private final ReturnRepository returnRepo;
    private final SmsService smsService;

    private static final int RETURN_SLA_DAYS = 12;

    @GetMapping("/order/{orderNumber}")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<Map<String, Object>>> getByOrder(@PathVariable String orderNumber) {
        Order order = orderRepo.findByOrderNumber(orderNumber).orElse(null);
        if (order == null) return ResponseEntity.status(404).body(ApiResponse.error("Order not found"));
        List<Return> returns = returnRepo.findByOrderId(order.getId());
        if (returns.isEmpty()) return ResponseEntity.ok(ApiResponse.ok(null));
        Return ret = returns.get(0);
        return ResponseEntity.ok(ApiResponse.ok(buildPublicRow(ret, orderNumber)));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> body) {
        String orderNumber = body.getOrDefault("orderNumber", "").toString().trim();
        String reason = body.getOrDefault("reason", "").toString().trim();

        Order order = orderRepo.findByOrderNumber(orderNumber).orElse(null);
        if (order == null) {
            return ResponseEntity.status(404).body(ApiResponse.error("Order not found"));
        }
        if (!"delivered".equalsIgnoreCase(order.getStatus())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Only delivered orders can be returned"));
        }
        if (order.getPlacedAt() != null) {
            OffsetDateTime deadline = order.getPlacedAt().plusDays(7);
            if (OffsetDateTime.now().isAfter(deadline)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Return window has expired"));
            }
        }
        if (!returnRepo.findByOrderId(order.getId()).isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("A return request already exists for this order"));
        }

        Return ret = new Return();
        ret.setOrder(order);
        ret.setReason(reason);
        ret.setStatus("requested");
        OffsetDateTime now = OffsetDateTime.now();
        ret.setRequestedAt(now);
        ret.setExpectedReturnBy(now.plusDays(RETURN_SLA_DAYS));
        ret = returnRepo.save(ret);

        String phone = order.getUser() != null ? order.getUser().getPhone()
                : order.getAddressSnapshot().getOrDefault("phone", null);
        smsService.sendOrderNotification(phone, orderNumber, OrderNotificationType.RETURN_INITIATED);

        return ResponseEntity.ok(ApiResponse.ok(buildPublicRow(ret, orderNumber)));
    }

    private Map<String, Object> buildPublicRow(Return ret, String orderNumber) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", ret.getId());
        m.put("orderNumber", orderNumber);
        m.put("status", ret.getStatus());
        m.put("reason", ret.getReason());
        m.put("requestedAt", ret.getRequestedAt());
        m.put("resolvedAt", ret.getResolvedAt());
        m.put("expectedReturnBy", ret.getExpectedReturnBy());
        m.put("refundAmount", ret.getRefundAmount());
        m.put("coinsRefunded", ret.getCoinsRefunded());
        return m;
    }
}
