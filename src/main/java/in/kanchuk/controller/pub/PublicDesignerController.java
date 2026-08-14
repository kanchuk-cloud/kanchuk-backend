package in.kanchuk.controller.pub;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.Designer;
import in.kanchuk.repository.DesignerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/designers")
@RequiredArgsConstructor
public class PublicDesignerController {

    private final DesignerRepository repo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Designer>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(
                repo.findByDeletedAtIsNullAndIsActiveTrueOrderBySortOrderAsc()));
    }
}
