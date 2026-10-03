package in.kanchuk.controller.admin;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.Testimonial;
import in.kanchuk.repository.TestimonialRepository;
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
@RequestMapping("/api/v1/admin/testimonials")
@RequiredArgsConstructor
public class AdminTestimonialController extends GenericAdminService {

    private final TestimonialRepository repo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Testimonial>>> list(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        Page<Testimonial> pg = search.isBlank()
                ? repo.findAll(pageRequest(page, limit))
                : repo.search(search, pageRequest(page, limit));
        return ResponseEntity.ok(ApiResponse.ok(pg.getContent(), buildMeta(pg, page, limit)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Testimonial>> create(@RequestBody Map<String, Object> body) {
        Testimonial e = new Testimonial();
        applyPatch(e, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(repo.save(e)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Testimonial>> update(
            @PathVariable UUID id, @RequestBody Map<String, Object> fields) {
        Testimonial e = findOrThrow(repo, id, "Testimonial");
        applyPatch(e, fields);
        return ResponseEntity.ok(ApiResponse.ok(repo.save(e)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        findOrThrow(repo, id, "Testimonial");
        repo.deleteById(id);
        return ResponseEntity.ok(ApiResponse.deleted());
    }
}
