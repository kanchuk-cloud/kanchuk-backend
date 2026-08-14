package in.kanchuk.controller.admin;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.Pincode;
import in.kanchuk.repository.PincodeRepository;
import in.kanchuk.service.GenericAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/pincodes")
@RequiredArgsConstructor
public class AdminPincodeController extends GenericAdminService {

    private final PincodeRepository repo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Pincode>>> list(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int limit) {
        PageRequest pr = PageRequest.of(page - 1, limit, Sort.by("pincode").ascending());
        Page<Pincode> pg = search.isBlank()
                ? repo.findAll(pr)
                : repo.findByPincodeContaining(search, pr);
        return ResponseEntity.ok(ApiResponse.ok(pg.getContent(), buildMeta(pg, page, limit)));
    }

    @GetMapping("/{pincode}")
    public ResponseEntity<ApiResponse<Pincode>> get(@PathVariable String pincode) {
        return repo.findById(pincode)
                .map(p -> ResponseEntity.ok(ApiResponse.ok(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Pincode>> create(@RequestBody Pincode body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(repo.save(body)));
    }

    @PutMapping("/{pincode}")
    public ResponseEntity<ApiResponse<Pincode>> update(@PathVariable String pincode, @RequestBody Map<String, Object> fields) {
        Pincode e = repo.findById(pincode)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Pincode not found: " + pincode));
        if (fields.containsKey("city")) e.setCity((String) fields.get("city"));
        if (fields.containsKey("state")) e.setState((String) fields.get("state"));
        if (fields.containsKey("estimatedDays")) e.setEstimatedDays((Integer) fields.get("estimatedDays"));
        if (fields.containsKey("hyperlocal")) e.setHyperlocal((Boolean) fields.get("hyperlocal"));
        if (fields.containsKey("isServiceable")) e.setServiceable((Boolean) fields.get("isServiceable"));
        return ResponseEntity.ok(ApiResponse.ok(repo.save(e)));
    }

    @DeleteMapping("/{pincode}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String pincode) {
        repo.deleteById(pincode);
        return ResponseEntity.ok(ApiResponse.deleted());
    }
}
