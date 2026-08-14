package in.kanchuk.controller.pub;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.Occasion;
import in.kanchuk.repository.OccasionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/occasions")
@RequiredArgsConstructor
public class PublicOccasionController {

    private final OccasionRepository repo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Occasion>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(
                repo.findByDeletedAtIsNullAndIsActiveTrueOrderBySortOrderAsc()));
    }
}
