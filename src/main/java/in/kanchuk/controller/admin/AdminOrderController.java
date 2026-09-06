package in.kanchuk.controller.admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.*;
import in.kanchuk.repository.*;
import in.kanchuk.service.GenericAdminService;
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
    private final ObjectMapper objectMapper;

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
    public ResponseEntity<ApiResponse<Order>> update(@PathVariable UUID id, @RequestBody Map<String, Object> fields) {
        Order e = findOrThrow(repo, id, "Order");
        applyPatch(e, fields);
        return ResponseEntity.ok(ApiResponse.ok(repo.save(e)));
    }

    // ── Advance status ────────────────────────────────────────────────────────

    @PatchMapping("/{id}/advance")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> advance(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, Object> body) {
        if (body == null) body = Map.of();
        Order order = findOrThrow(repo, id, "Order");

        switch (order.getStatus()) {
            case "placed" -> order.setStatus("confirmed");

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
            }

            case "shipped" -> {
                fulfillmentRepo.findByOrderId(id).stream().findFirst().ifPresent(f -> {
                    f.setStatus("out_for_delivery");
                    fulfillmentRepo.save(f);
                });
                order.setStatus("out_for_delivery");
            }

            case "out_for_delivery" -> {
                fulfillmentRepo.findByOrderId(id).stream().findFirst().ifPresent(f -> {
                    f.setStatus("delivered");
                    f.setDeliveredAt(OffsetDateTime.now());
                    fulfillmentRepo.save(f);
                });
                order.setStatus("delivered");
            }

            default -> {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Cannot advance order with status: " + order.getStatus()));
            }
        }

        repo.save(order);
        return ResponseEntity.ok(ApiResponse.ok(buildOrderMap(order)));
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
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(returnRepo.save(r)));
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
        return m;
    }

    private Map<String, Object> buildOrderMap(Order o) {
        UUID id = o.getId();
        Map<String, Object> m = buildListRow(o);
        m.put("subtotal", o.getSubtotal());
        m.put("discountAmount", o.getDiscountAmount());
        m.put("deliveryCharge", o.getDeliveryCharge());

        // Items
        m.put("items", itemRepo.findByOrderId(id).stream().map(item -> {
            Map<String, Object> im = new LinkedHashMap<>();
            im.put("id", item.getId());
            im.put("price", item.getPrice());
            im.put("quantity", item.getQuantity());
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
