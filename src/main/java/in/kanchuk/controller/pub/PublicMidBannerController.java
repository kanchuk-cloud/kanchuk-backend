package in.kanchuk.controller.pub;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.MidBanner;
import in.kanchuk.repository.MidBannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/mid-banners")
@RequiredArgsConstructor
public class PublicMidBannerController {

    private final MidBannerRepository repo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MidBanner>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(repo.findByIsActiveTrueOrderBySortOrderAsc()));
    }
}
