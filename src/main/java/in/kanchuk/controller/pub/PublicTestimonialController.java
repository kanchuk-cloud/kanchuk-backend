package in.kanchuk.controller.pub;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.Testimonial;
import in.kanchuk.repository.TestimonialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/testimonials")
@RequiredArgsConstructor
public class PublicTestimonialController {

    private final TestimonialRepository repo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Testimonial>>> list() {
        List<Testimonial> items = repo.findByIsActiveTrueOrderByDisplayOrderAsc();
        if (items.size() > 20) items = items.subList(0, 20);
        return ResponseEntity.ok(ApiResponse.ok(items));
    }
}
