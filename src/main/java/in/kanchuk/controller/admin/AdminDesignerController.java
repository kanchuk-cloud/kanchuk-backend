package in.kanchuk.controller.admin;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.Designer;
import in.kanchuk.repository.DesignerRepository;
import in.kanchuk.service.GenericAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/designers")
@RequiredArgsConstructor
public class AdminDesignerController extends GenericAdminService {

    private final DesignerRepository repo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Designer>>> list(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        Page<Designer> pg = search.isBlank()
                ? repo.findByDeletedAtIsNull(pageRequest(page, limit))
                : repo.findByDeletedAtIsNullAndNameContainingIgnoreCase(search, pageRequest(page, limit));
        return ResponseEntity.ok(ApiResponse.ok(pg.getContent(), buildMeta(pg, page, limit)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Designer>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(findOrThrow(repo, id, "Designer")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Designer>> create(@RequestBody Designer body) {
        body.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(repo.save(body)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Designer>> update(@PathVariable UUID id, @RequestBody Map<String, Object> fields) {
        Designer e = findOrThrow(repo, id, "Designer");
        applyPatch(e, fields);
        return ResponseEntity.ok(ApiResponse.ok(repo.save(e)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        Designer e = findOrThrow(repo, id, "Designer");
        e.softDelete();
        repo.save(e);
        return ResponseEntity.ok(ApiResponse.deleted());
    }
}
