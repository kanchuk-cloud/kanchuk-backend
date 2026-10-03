package in.kanchuk.controller.pub;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.SavingsEditCard;
import in.kanchuk.repository.SavingsEditCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/savings-edit")
@RequiredArgsConstructor
public class PublicSavingsEditController {

    private final SavingsEditCardRepository repo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SavingsEditCard>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(repo.findByIsActiveTrueOrderBySortOrderAsc()));
    }
}
