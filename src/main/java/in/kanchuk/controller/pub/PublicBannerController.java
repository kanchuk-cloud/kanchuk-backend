package in.kanchuk.controller.pub;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.Banner;
import in.kanchuk.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/banners")
@RequiredArgsConstructor
public class PublicBannerController {

    private final BannerRepository repo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Banner>>> list(
            @RequestParam(required = false) String placement) {
        List<Banner> banners = repo.findByDeletedAtIsNullAndIsActiveTrueOrderBySortOrderAsc();
        if (placement != null && !placement.isBlank()) {
            banners = banners.stream()
                    .filter(b -> placement.equalsIgnoreCase(b.getPlacement()))
                    .toList();
        }
        return ResponseEntity.ok(ApiResponse.ok(banners));
    }
}
