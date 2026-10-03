package in.kanchuk.controller.admin;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.SavingsEditCard;
import in.kanchuk.repository.SavingsEditCardRepository;
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
@RequestMapping("/api/v1/admin/savings-edit")
@RequiredArgsConstructor
public class AdminSavingsEditController extends GenericAdminService {

    private final SavingsEditCardRepository repo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SavingsEditCard>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        Page<SavingsEditCard> pg = repo.findAll(pageRequest(page, limit));
        return ResponseEntity.ok(ApiResponse.ok(pg.getContent(), buildMeta(pg, page, limit)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SavingsEditCard>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(findOrThrow(repo, id, "SavingsEditCard")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SavingsEditCard>> create(@RequestBody SavingsEditCard body) {
        body.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(repo.save(body)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SavingsEditCard>> update(@PathVariable UUID id, @RequestBody Map<String, Object> fields) {
        SavingsEditCard e = findOrThrow(repo, id, "SavingsEditCard");
        applyPatch(e, fields);
        return ResponseEntity.ok(ApiResponse.ok(repo.save(e)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        repo.deleteById(id);
        return ResponseEntity.ok(ApiResponse.deleted());
    }
}
