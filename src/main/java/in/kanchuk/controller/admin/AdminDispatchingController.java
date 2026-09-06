package in.kanchuk.controller.admin;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.Dispatching;
import in.kanchuk.repository.DispatchingRepository;
import in.kanchuk.repository.FulfillmentRepository;
import in.kanchuk.repository.OrderRepository;
import in.kanchuk.repository.PackagingRepository;
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
@RequestMapping("/api/v1/admin/dispatching")
@RequiredArgsConstructor
public class AdminDispatchingController extends GenericAdminService {

    private final DispatchingRepository repo;
    private final OrderRepository orderRepo;
    private final PackagingRepository packagingRepo;
    private final FulfillmentRepository fulfillmentRepo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Dispatching>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        Page<Dispatching> pg = repo.findAll(pageRequest(page, limit));
        return ResponseEntity.ok(ApiResponse.ok(pg.getContent(), buildMeta(pg, page, limit)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Dispatching>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(findOrThrow(repo, id, "Dispatching")));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<Dispatching>> create(@RequestBody Map<String, Object> body) {
        Dispatching e = new Dispatching();
        if (body.containsKey("orderId") && body.get("orderId") != null) {
            orderRepo.findById(UUID.fromString(body.get("orderId").toString()))
                    .ifPresent(e::setOrder);
        }
        if (body.containsKey("packagingId") && body.get("packagingId") != null) {
            packagingRepo.findById(UUID.fromString(body.get("packagingId").toString()))
                    .ifPresent(e::setPackaging);
        }
        if (body.containsKey("fulfillmentId") && body.get("fulfillmentId") != null) {
            fulfillmentRepo.findById(UUID.fromString(body.get("fulfillmentId").toString()))
                    .ifPresent(e::setFulfillment);
        }
        applyPatch(e, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(repo.save(e)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Dispatching>> update(@PathVariable UUID id, @RequestBody Map<String, Object> fields) {
        Dispatching e = findOrThrow(repo, id, "Dispatching");
        applyPatch(e, fields);
        return ResponseEntity.ok(ApiResponse.ok(repo.save(e)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        repo.deleteById(id);
        return ResponseEntity.ok(ApiResponse.deleted());
    }
}
