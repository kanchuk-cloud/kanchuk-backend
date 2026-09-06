package in.kanchuk.controller.admin;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.ImageType;
import in.kanchuk.repository.ImageTypeRepository;
import in.kanchuk.service.GenericAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/image-types")
@RequiredArgsConstructor
public class AdminImageTypeController extends GenericAdminService {

    private final ImageTypeRepository repo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ImageType>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(repo.findAllByOrderBySortOrderAsc()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ImageType>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(findOrThrow(repo, id, "ImageType")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ImageType>> create(@RequestBody Map<String, Object> body) {
        String value = body.getOrDefault("value", "").toString();
        if (!value.isBlank() && repo.existsByValue(value)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Image type with value '" + value + "' already exists"));
        }
        ImageType e = new ImageType();
        applyPatch(e, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(repo.save(e)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ImageType>> update(@PathVariable UUID id,
                                                         @RequestBody Map<String, Object> body) {
        ImageType e = findOrThrow(repo, id, "ImageType");
        applyPatch(e, body);
        return ResponseEntity.ok(ApiResponse.ok(repo.save(e)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        repo.deleteById(id);
        return ResponseEntity.ok(ApiResponse.deleted());
    }
}
