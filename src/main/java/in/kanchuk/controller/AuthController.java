package in.kanchuk.controller;

import in.kanchuk.dto.request.LoginRequest;
import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.dto.response.TokenResponse;
import in.kanchuk.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/admin/login")
    public ResponseEntity<ApiResponse<TokenResponse>> adminLogin(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(authService.loginAdmin(req)));
    }

    @PostMapping("/vendor/login")
    public ResponseEntity<ApiResponse<TokenResponse>> vendorLogin(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(authService.loginVendor(req)));
    }
}
