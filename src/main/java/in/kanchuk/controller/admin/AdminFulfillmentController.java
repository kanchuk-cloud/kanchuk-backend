package in.kanchuk.controller.admin;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.Fulfillment;
import in.kanchuk.repository.FulfillmentRepository;
import in.kanchuk.repository.OrderRepository;
import in.kanchuk.service.GenericAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/fulfillments")
@RequiredArgsConstructor
public class AdminFulfillmentController extends GenericAdminService {

    private final FulfillmentRepository repo;
    private final OrderRepository orderRepo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Fulfillment>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        Page<Fulfillment> pg = repo.findAll(pageRequest(page, limit));
        return ResponseEntity.ok(ApiResponse.ok(pg.getContent(), buildMeta(pg, page, limit)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Fulfillment>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(findOrThrow(repo, id, "Fulfillment")));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<Fulfillment>> create(@RequestBody Map<String, Object> body) {
        Fulfillment e = new Fulfillment();
        if (body.containsKey("orderId") && body.get("orderId") != null) {
            orderRepo.findById(UUID.fromString(body.get("orderId").toString()))
                    .ifPresent(e::setOrder);
        }
        applyPatch(e, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(repo.save(e)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Fulfillment>> update(@PathVariable UUID id, @RequestBody Map<String, Object> fields) {
        Fulfillment e = findOrThrow(repo, id, "Fulfillment");
        applyPatch(e, fields);
        return ResponseEntity.ok(ApiResponse.ok(repo.save(e)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        repo.deleteById(id);
        return ResponseEntity.ok(ApiResponse.deleted());
    }
}
