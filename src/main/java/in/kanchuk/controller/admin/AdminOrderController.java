package in.kanchuk.controller.admin;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.Order;
import in.kanchuk.repository.OrderRepository;
import in.kanchuk.service.GenericAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController extends GenericAdminService {

    private final OrderRepository repo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Order>>> list(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        Page<Order> pg = search.isBlank()
                ? repo.findAll(pageRequest(page, limit))
                : repo.searchOrders(search, pageRequest(page, limit));
        return ResponseEntity.ok(ApiResponse.ok(pg.getContent(), buildMeta(pg, page, limit)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Order>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(findOrThrow(repo, id, "Order")));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Order>> update(@PathVariable UUID id, @RequestBody Map<String, Object> fields) {
        Order e = findOrThrow(repo, id, "Order");
        applyPatch(e, fields);
        return ResponseEntity.ok(ApiResponse.ok(repo.save(e)));
    }
}
