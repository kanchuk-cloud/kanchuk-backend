package in.kanchuk.service;

import in.kanchuk.dto.request.LoginRequest;
import in.kanchuk.dto.response.TokenResponse;
import in.kanchuk.entity.AdminUser;
import in.kanchuk.entity.OtpPurpose;
import in.kanchuk.entity.User;
import in.kanchuk.entity.VendorUser;
import in.kanchuk.repository.AdminUserRepository;
import in.kanchuk.repository.UserRepository;
import in.kanchuk.repository.VendorUserRepository;
import in.kanchuk.security.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AdminUserRepository adminUserRepository;
    private final VendorUserRepository vendorUserRepository;
    private final UserRepository userRepository;
    private final OtpService otpService;
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

    public TokenResponse registerCustomer(Map<String, Object> body) {
        String email = (String) body.get("email");
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = new User();
        user.setName((String) body.get("name"));
        user.setEmail(email);
        user.setPhone((String) body.get("phone"));
        user.setPasswordHash(passwordEncoder.encode((String) body.get("password")));
        user.setActive(true);
        userRepository.save(user);
        String token = jwtUtil.generateToken(email, "CUSTOMER");
        return new TokenResponse(token, "CUSTOMER", email, user.getName());
    }

    public TokenResponse loginCustomer(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("Invalid credentials"));
        if (!user.isActive()) {
            throw new IllegalArgumentException("Account is disabled");
        }
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new EntityNotFoundException("Invalid credentials");
        }
        String token = jwtUtil.generateToken(user.getEmail(), "CUSTOMER");
        return new TokenResponse(token, "CUSTOMER", user.getEmail(), user.getName());
    }

    public TokenResponse loginCustomerByOtp(String mobileNumber, String otp) {
        otpService.verifyOtp(mobileNumber, otp, OtpPurpose.LOGIN);

        User user = userRepository.findByPhone(mobileNumber).orElseGet(() -> {
            User newUser = new User();
            newUser.setPhone(mobileNumber);
            newUser.setName("User" + mobileNumber.substring(mobileNumber.length() - 4));
            newUser.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
            newUser.setActive(true);
            return userRepository.save(newUser);
        });

        if (!user.isActive()) {
            throw new IllegalArgumentException("Account is disabled");
        }

        // Use phone as the JWT subject for phone-only accounts
        String subject = user.getEmail() != null ? user.getEmail() : mobileNumber;
        String token = jwtUtil.generateToken(subject, "CUSTOMER");
        return new TokenResponse(token, "CUSTOMER", subject, user.getName(), mobileNumber, user.getId().toString());
    }
}
