package in.kanchuk.controller.admin;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.AdminUser;
import in.kanchuk.repository.AdminUserRepository;
import in.kanchuk.service.GenericAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController extends GenericAdminService {

    private final AdminUserRepository repo;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminUser>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        Page<AdminUser> pg = repo.findAll(pageRequest(page, limit));
        return ResponseEntity.ok(ApiResponse.ok(pg.getContent(), buildMeta(pg, page, limit)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminUser>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(findOrThrow(repo, id, "AdminUser")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdminUser>> create(@RequestBody AdminUser body) {
        body.setId(null);
        body.setPasswordHash(passwordEncoder.encode(body.getPasswordHash()));
        return ResponseEntity.ok(ApiResponse.created(repo.save(body)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminUser>> update(@PathVariable UUID id, @RequestBody Map<String, Object> fields) {
        AdminUser e = findOrThrow(repo, id, "AdminUser");
        if (fields.containsKey("name")) e.setName((String) fields.get("name"));
        if (fields.containsKey("isActive")) e.setActive((Boolean) fields.get("isActive"));
        if (fields.containsKey("role")) e.setRole((String) fields.get("role"));
        if (fields.containsKey("password")) e.setPasswordHash(passwordEncoder.encode((String) fields.get("password")));
        return ResponseEntity.ok(ApiResponse.ok(repo.save(e)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        AdminUser e = findOrThrow(repo, id, "AdminUser");
        e.softDelete();
        repo.save(e);
        return ResponseEntity.ok(ApiResponse.deleted());
    }
}
