package in.kanchuk.controller.pub;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.Category;
import in.kanchuk.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/categories")
@RequiredArgsConstructor
public class PublicCategoryController {

    private final CategoryRepository repo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> list() {
        List<Category> categories = repo.findByDeletedAtIsNullAndIsActiveTrue();
        return ResponseEntity.ok(ApiResponse.ok(categories));
    }
}
