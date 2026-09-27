package in.kanchuk.controller;

import in.kanchuk.dto.request.LoginRequest;
import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.dto.response.TokenResponse;
import in.kanchuk.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    @PostMapping("/customer/register")
    public ResponseEntity<ApiResponse<TokenResponse>> customerRegister(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(authService.registerCustomer(body)));
    }

    @PostMapping("/customer/login")
    public ResponseEntity<ApiResponse<TokenResponse>> customerLogin(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(authService.loginCustomer(req)));
    }

    @PostMapping("/customer/otp-login")
    public ResponseEntity<ApiResponse<TokenResponse>> customerOtpLogin(@RequestBody Map<String, Object> body) {
        String mobileNumber = (String) body.get("mobileNumber");
        String otp = (String) body.get("otp");
        return ResponseEntity.ok(ApiResponse.ok(authService.loginCustomerByOtp(mobileNumber, otp)));
    }
}
