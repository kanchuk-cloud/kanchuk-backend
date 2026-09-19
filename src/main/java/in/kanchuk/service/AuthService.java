package in.kanchuk.service;

import in.kanchuk.dto.request.LoginRequest;
import in.kanchuk.dto.response.TokenResponse;
import in.kanchuk.entity.AdminUser;
import in.kanchuk.entity.VendorUser;
import in.kanchuk.repository.AdminUserRepository;
import in.kanchuk.repository.VendorUserRepository;
import in.kanchuk.security.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AdminUserRepository adminUserRepository;
    private final VendorUserRepository vendorUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public TokenResponse loginAdmin(LoginRequest req) {
        AdminUser admin = adminUserRepository
                .findByEmailAndDeletedAtIsNull(req.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("Invalid credentials"));

        if (!admin.isActive()) {
            throw new IllegalArgumentException("Account is disabled");
        }

        if (!passwordEncoder.matches(req.getPassword(), admin.getPasswordHash())) {
            throw new EntityNotFoundException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(admin.getEmail(), "ADMIN");
        return new TokenResponse(token, "ADMIN", admin.getEmail(), admin.getName());
    }

    public TokenResponse loginVendor(LoginRequest req) {
        VendorUser vendor = vendorUserRepository
                .findByEmailAndDeletedAtIsNull(req.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("Invalid credentials"));

        if (!vendor.isActive()) {
            throw new IllegalArgumentException("Account is disabled");
        }

        if (!passwordEncoder.matches(req.getPassword(), vendor.getPasswordHash())) {
            throw new EntityNotFoundException("Invalid credentials");
        }

        String token = jwtUtil.generateVendorToken(vendor.getEmail(), vendor.getSeller().getId().toString());
        return new TokenResponse(token, "VENDOR", vendor.getEmail(), vendor.getName());
    }
}
