package in.kanchuk.controller.pub;

import in.kanchuk.dto.request.SendOtpRequest;
import in.kanchuk.dto.request.VerifyOtpRequest;
import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.OtpPurpose;
import in.kanchuk.service.OtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class PublicOtpController {

    private final OtpService otpService;

    /**
     * POST /api/v1/auth/send-otp
     * Generates a 6-digit OTP, stores a BCrypt hash, and dispatches via SMS.
     * The OTP itself is never returned in the response.
     */
    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<Void>> sendOtp(@Valid @RequestBody SendOtpRequest req) {
        otpService.sendOtp(req.getMobileNumber(), parsePurpose(req.getPurpose()));
        return ResponseEntity.ok(new ApiResponse<>(true, "OTP sent successfully", null, null));
    }

    /**
     * POST /api/v1/auth/verify-otp
     * Verifies the OTP. Returns 400 for invalid, expired, or already-used OTPs.
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(@Valid @RequestBody VerifyOtpRequest req) {
        otpService.verifyOtp(req.getMobileNumber(), req.getOtp(), parsePurpose(req.getPurpose()));
        return ResponseEntity.ok(new ApiResponse<>(true, "OTP verified successfully", null, null));
    }

    private OtpPurpose parsePurpose(String purpose) {
        if (purpose == null || purpose.isBlank()) return OtpPurpose.LOGIN;
        try {
            return OtpPurpose.valueOf(purpose.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid purpose. Accepted values: LOGIN, SIGNUP, PHONE_VERIFICATION, FORGOT_PASSWORD");
        }
    }
}
