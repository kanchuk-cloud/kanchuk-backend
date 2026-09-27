package in.kanchuk.controller.admin;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.User;
import in.kanchuk.repository.UserRepository;
import in.kanchuk.service.GenericAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/customers")
@RequiredArgsConstructor
public class AdminCustomerController extends GenericAdminService {

    private final UserRepository repo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String search) {
        Page<User> pg = (search != null && !search.isBlank())
                ? repo.searchCustomers(search.trim(), pageRequest(page, limit))
                : repo.findByDeletedAtIsNull(pageRequest(page, limit));
        return ResponseEntity.ok(ApiResponse.ok(pg.getContent(), buildMeta(pg, page, limit)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(findOrThrow(repo, id, "Customer")));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> update(@PathVariable UUID id, @RequestBody Map<String, Object> fields) {
        User u = findOrThrow(repo, id, "Customer");
        applyPatch(u, fields);
        return ResponseEntity.ok(ApiResponse.ok(repo.save(u)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        User u = findOrThrow(repo, id, "Customer");
        u.softDelete();
        repo.save(u);
        return ResponseEntity.ok(ApiResponse.deleted());
    }
}
