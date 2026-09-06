package in.kanchuk.controller.admin;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.Return;
import in.kanchuk.repository.OrderItemRepository;
import in.kanchuk.repository.OrderRepository;
import in.kanchuk.repository.ReturnRepository;
import in.kanchuk.service.GenericAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/returns")
@RequiredArgsConstructor
public class AdminReturnController extends GenericAdminService {

    private final ReturnRepository repo;
    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Return>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        Page<Return> pg = repo.findAll(pageRequest(page, limit));
        return ResponseEntity.ok(ApiResponse.ok(pg.getContent(), buildMeta(pg, page, limit)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Return>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(findOrThrow(repo, id, "Return")));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<Return>> create(@RequestBody Map<String, Object> body) {
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
        e.setRequestedAt(OffsetDateTime.now());
        if (body.containsKey("reason") && body.get("reason") != null) {
            e.setReason(body.get("reason").toString());
        }
        if (body.containsKey("refundAmount") && body.get("refundAmount") != null) {
            e.setRefundAmount(new BigDecimal(body.get("refundAmount").toString()));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(repo.save(e)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Return>> update(@PathVariable UUID id, @RequestBody Map<String, Object> fields) {
        Return e = findOrThrow(repo, id, "Return");
        applyPatch(e, fields);
        return ResponseEntity.ok(ApiResponse.ok(repo.save(e)));
    }
}
