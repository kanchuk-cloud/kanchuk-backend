package in.kanchuk.controller.pub;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.SizeChart;
import in.kanchuk.repository.SizeChartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/size-charts")
@RequiredArgsConstructor
public class PublicSizeChartController {

    private final SizeChartRepository repo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SizeChart>>> listByCategory(
            @RequestParam String categorySlug) {
        List<SizeChart> charts = repo.findByCategory_SlugAndDeletedAtIsNullAndIsActiveTrue(categorySlug);
        return ResponseEntity.ok(ApiResponse.ok(charts));
    }
}
