package in.kanchuk.controller.admin;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.Seller;
import in.kanchuk.entity.VendorUser;
import in.kanchuk.repository.SellerRepository;
import in.kanchuk.repository.VendorUserRepository;
import in.kanchuk.service.GenericAdminService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/vendor-portal")
@RequiredArgsConstructor
public class AdminVendorPortalController extends GenericAdminService {

    private final VendorUserRepository vendorUserRepo;
    private final SellerRepository sellerRepo;
    private final PasswordEncoder passwordEncoder;

    // ── List users for a seller ────────────────────────────────────────────

    @GetMapping("/sellers/{sellerId}/users")
    public ResponseEntity<ApiResponse<List<VendorUser>>> listUsers(
            @PathVariable UUID sellerId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        Page<VendorUser> pg = vendorUserRepo.findBySeller_IdAndDeletedAtIsNull(sellerId, pageRequest(page, limit));
        return ResponseEntity.ok(ApiResponse.ok(pg.getContent(), buildMeta(pg, page, limit)));
    }

    // ── Create vendor portal user ──────────────────────────────────────────

    @PostMapping("/sellers/{sellerId}/users")
    public ResponseEntity<ApiResponse<VendorUser>> createUser(
            @PathVariable UUID sellerId,
            @RequestBody Map<String, String> body) {
        Seller seller = sellerRepo.findById(sellerId)
                .orElseThrow(() -> new EntityNotFoundException("Seller not found: " + sellerId));

        String email = body.get("email");
        String name = body.get("name");
        String password = body.get("password");

        if (email == null || name == null || password == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("email, name, and password are required"));
        }

        if (vendorUserRepo.existsByEmailAndDeletedAtIsNull(email)) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Email already in use"));
        }

        VendorUser user = new VendorUser();
        user.setSeller(seller);
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setActive(true);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(vendorUserRepo.save(user)));
    }

    // ── Toggle active ──────────────────────────────────────────────────────

    @PutMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<VendorUser>> updateUser(
            @PathVariable UUID userId,
            @RequestBody Map<String, Object> fields) {
        VendorUser user = findOrThrow(vendorUserRepo, userId, "VendorUser");
        if (fields.containsKey("isActive")) {
            user.setActive(Boolean.parseBoolean(fields.get("isActive").toString()));
        }
        if (fields.containsKey("name")) {
            user.setName(fields.get("name").toString());
        }
        if (fields.containsKey("password")) {
            user.setPasswordHash(passwordEncoder.encode(fields.get("password").toString()));
        }
        return ResponseEntity.ok(ApiResponse.ok(vendorUserRepo.save(user)));
    }

    // ── Soft delete ────────────────────────────────────────────────────────

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID userId) {
        VendorUser user = findOrThrow(vendorUserRepo, userId, "VendorUser");
        user.softDelete();
        vendorUserRepo.save(user);
        return ResponseEntity.ok(ApiResponse.deleted());
    }
}
