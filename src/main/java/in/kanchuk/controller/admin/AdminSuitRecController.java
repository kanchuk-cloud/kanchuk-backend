package in.kanchuk.controller.admin;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.SuitStylingRecommendation;
import in.kanchuk.repository.SuitStylingRecommendationRepository;
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
@RequestMapping("/api/v1/admin/suit-recommendations")
@RequiredArgsConstructor
public class AdminSuitRecController extends GenericAdminService {

    private final SuitStylingRecommendationRepository repo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SuitStylingRecommendation>>> list(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        Page<SuitStylingRecommendation> pg = search.isBlank()
                ? repo.findAll(pageRequest(page, limit))
                : repo.findByStyleNameContainingIgnoreCase(search, pageRequest(page, limit));
        return ResponseEntity.ok(ApiResponse.ok(pg.getContent(), buildMeta(pg, page, limit)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SuitStylingRecommendation>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(findOrThrow(repo, id, "SuitStylingRecommendation")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SuitStylingRecommendation>> create(@RequestBody SuitStylingRecommendation body) {
        body.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(repo.save(body)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SuitStylingRecommendation>> update(@PathVariable UUID id, @RequestBody Map<String, Object> fields) {
        SuitStylingRecommendation e = findOrThrow(repo, id, "SuitStylingRecommendation");
        applyPatch(e, fields);
        return ResponseEntity.ok(ApiResponse.ok(repo.save(e)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        repo.deleteById(id);
        return ResponseEntity.ok(ApiResponse.deleted());
    }
}
