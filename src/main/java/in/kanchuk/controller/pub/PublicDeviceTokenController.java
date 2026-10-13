package in.kanchuk.controller.pub;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.DeviceToken;
import in.kanchuk.repository.DeviceTokenRepository;
import in.kanchuk.service.GenericAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/public/device-tokens")
@RequiredArgsConstructor
public class PublicDeviceTokenController extends GenericAdminService {

    private final DeviceTokenRepository repo;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> register(@RequestBody Map<String, Object> body) {
        String userId = body.getOrDefault("userId", "").toString().trim();
        String token  = body.getOrDefault("token",  "").toString().trim();
        String platform = body.getOrDefault("platform", "").toString().trim();

        if (userId.isBlank() || token.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("userId and token are required"));
        }

        repo.findByToken(token).ifPresentOrElse(
            existing -> {
                existing.setUserId(userId);
                repo.save(existing);
            },
            () -> {
                DeviceToken dt = new DeviceToken();
                dt.setUserId(userId);
                dt.setToken(token);
                dt.setPlatform(platform.isBlank() ? null : platform);
                repo.save(dt);
            }
        );

        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
