package in.kanchuk.controller.admin;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.MediaAsset;
import in.kanchuk.repository.MediaAssetRepository;
import in.kanchuk.service.GenericAdminService;
import in.kanchuk.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/media")
@RequiredArgsConstructor
public class AdminMediaController extends GenericAdminService {

    private final StorageService storageService;
    private final MediaAssetRepository repo;

    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg", "image/png", "image/webp", "image/gif", "image/svg+xml"
    );

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<MediaAsset>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") String category,
            @RequestParam("subcategory") String subcategory) throws IOException {

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Only JPEG, PNG, WebP, GIF and SVG images are allowed."));
        }

        String url = storageService.store(file, category, subcategory);

        MediaAsset asset = new MediaAsset();
        asset.setCategory(category);
        asset.setSubcategory(subcategory);
        asset.setFilename(url.substring(url.lastIndexOf('/') + 1));
        asset.setOriginalName(file.getOriginalFilename());
        asset.setUrl(url);
        asset.setSizeBytes(file.getSize());
        asset.setContentType(contentType);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(repo.save(asset)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MediaAsset>>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String subcategory,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "50") int limit) {

        Page<MediaAsset> pg;
        if (category != null && subcategory != null) {
            pg = repo.findByCategoryAndSubcategory(category, subcategory, pageRequest(page, limit));
        } else if (category != null) {
            pg = repo.findByCategory(category, pageRequest(page, limit));
        } else {
            pg = repo.findAll(pageRequest(page, limit));
        }

        return ResponseEntity.ok(ApiResponse.ok(pg.getContent(), buildMeta(pg, page, limit)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) throws IOException {
        MediaAsset asset = findOrThrow(repo, id, "MediaAsset");
        storageService.delete(asset.getUrl());
        repo.delete(asset);
        return ResponseEntity.ok(ApiResponse.deleted());
    }
}
