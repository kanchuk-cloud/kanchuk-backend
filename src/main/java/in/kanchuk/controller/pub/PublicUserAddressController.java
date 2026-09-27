package in.kanchuk.controller.pub;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.User;
import in.kanchuk.entity.UserAddress;
import in.kanchuk.repository.UserAddressRepository;
import in.kanchuk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/public/user-addresses")
@RequiredArgsConstructor
public class PublicUserAddressController {

    private final UserRepository userRepo;
    private final UserAddressRepository addressRepo;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> list(@RequestParam String phone) {
        Optional<User> userOpt = userRepo.findByPhone(phone.trim());
        if (userOpt.isEmpty()) return ResponseEntity.ok(ApiResponse.ok(List.of()));
        List<UserAddress> addresses = addressRepo.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userOpt.get().getId());
        return ResponseEntity.ok(ApiResponse.ok(addresses.stream().map(this::toMap).toList()));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> create(@RequestBody Map<String, Object> body) {
        String phone = body.getOrDefault("phone", "").toString().trim();
        User user = userRepo.findByPhone(phone)
                .orElseThrow(() -> new IllegalArgumentException("User not found for phone: " + phone));

        boolean isDefault = Boolean.parseBoolean(body.getOrDefault("isDefault", "false").toString());
        if (isDefault) {
            addressRepo.clearDefaultForUser(user.getId());
        }

        UserAddress addr = new UserAddress();
        addr.setUser(user);
        addr.setName(body.getOrDefault("name", "").toString());
        addr.setPhone(phone);
        addr.setLine1(body.getOrDefault("line1", "").toString());
        Object line2 = body.get("line2");
        addr.setLine2(line2 != null && !line2.toString().isBlank() ? line2.toString() : null);
        addr.setCity(body.getOrDefault("city", "").toString());
        addr.setState(body.getOrDefault("state", "").toString());
        addr.setPincode(body.getOrDefault("pincode", "").toString());
        addr.setLabel(body.getOrDefault("label", "HOME").toString());
        addr.setDefault(isDefault);

        addr = addressRepo.save(addr);
        return ResponseEntity.ok(ApiResponse.ok(toMap(addr)));
    }

    @PutMapping("/{id}/default")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> setDefault(
            @PathVariable UUID id, @RequestParam String phone) {
        User user = userRepo.findByPhone(phone.trim())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        UserAddress addr = addressRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));
        if (!addr.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(ApiResponse.error("Not authorized"));
        }
        addressRepo.clearDefaultForUser(user.getId());
        addr.setDefault(true);
        addressRepo.save(addr);
        return ResponseEntity.ok(ApiResponse.ok(toMap(addr)));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID id, @RequestParam String phone) {
        User user = userRepo.findByPhone(phone.trim())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        UserAddress addr = addressRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));
        if (!addr.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(ApiResponse.error("Not authorized"));
        }
        addressRepo.delete(addr);
        return ResponseEntity.ok(ApiResponse.deleted());
    }

    private Map<String, Object> toMap(UserAddress a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", a.getId());
        m.put("name", a.getName());
        m.put("phone", a.getPhone());
        m.put("line1", a.getLine1());
        m.put("line2", a.getLine2());
        m.put("city", a.getCity());
        m.put("state", a.getState());
        m.put("pincode", a.getPincode());
        m.put("label", a.getLabel());
        m.put("isDefault", a.isDefault());
        return m;
    }
}
