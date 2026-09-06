package in.kanchuk.controller.admin;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.DeliveryZone;
import in.kanchuk.entity.Pincode;
import in.kanchuk.repository.DeliveryZoneRepository;
import in.kanchuk.repository.PincodeRepository;
import in.kanchuk.service.GenericAdminService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/v1/admin/pincodes")
@RequiredArgsConstructor
public class AdminPincodeController extends GenericAdminService {

    private final PincodeRepository repo;
    private final DeliveryZoneRepository zoneRepo;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Pincode>>> list(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String city,
            @RequestParam(defaultValue = "") String state,
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
    public ResponseEntity<ApiResponse<Pincode>> create(@RequestBody Map<String, Object> body) {
        String pin = Objects.toString(body.get("pincode"), "").trim();
        if (!pin.matches("\\d{6}")) {
            throw new IllegalArgumentException("Pincode must be exactly 6 digits.");
        }
        if (repo.existsById(pin)) {
            throw new IllegalArgumentException("Pincode already exists: " + pin);
        }
        Pincode p = new Pincode();
        p.setPincode(pin);
        p.setCity(Objects.toString(body.get("city"), null));
        p.setState(Objects.toString(body.get("state"), null));
        if (body.get("estimatedDays") != null) {
            p.setEstimatedDays(Integer.valueOf(body.get("estimatedDays").toString()));
        }
        if (body.get("hyperlocal") != null) {
            p.setHyperlocal(Boolean.parseBoolean(body.get("hyperlocal").toString()));
        }
        if (body.get("isServiceable") != null) {
            p.setServiceable(Boolean.parseBoolean(body.get("isServiceable").toString()));
        }
        resolveZone(p, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(repo.save(p)));
    }

    @PutMapping("/{pincode}")
    public ResponseEntity<ApiResponse<Pincode>> update(
            @PathVariable String pincode, @RequestBody Map<String, Object> fields) {
        Pincode e = repo.findById(pincode)
            .orElseThrow(() -> new EntityNotFoundException("Pincode not found: " + pincode));
        if (fields.containsKey("city"))         e.setCity((String) fields.get("city"));
        if (fields.containsKey("state"))        e.setState((String) fields.get("state"));
        if (fields.containsKey("estimatedDays")) {
            e.setEstimatedDays(fields.get("estimatedDays") != null
                ? Integer.valueOf(fields.get("estimatedDays").toString()) : null);
        }
        if (fields.containsKey("hyperlocal")) {
            e.setHyperlocal(Boolean.parseBoolean(fields.get("hyperlocal").toString()));
        }
        if (fields.containsKey("isServiceable")) {
            e.setServiceable(Boolean.parseBoolean(fields.get("isServiceable").toString()));
        }
        resolveZone(e, fields);
        return ResponseEntity.ok(ApiResponse.ok(repo.save(e)));
    }

    @DeleteMapping("/{pincode}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String pincode) {
        repo.deleteById(pincode);
        return ResponseEntity.ok(ApiResponse.deleted());
    }

    /**
     * Bulk import from CSV.
     * Expected format (header required): pincode,city,state,zone_code[,serviceable]
     * Validates every row; imports only valid rows. Returns a summary.
     */
    @PostMapping("/import")
    public ResponseEntity<ApiResponse<Map<String, Object>>> importCsv(
            @RequestParam("file") MultipartFile file) throws IOException {
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        String[] lines = content.split("\\r?\\n");

        int imported = 0;
        List<String> failures = new ArrayList<>();

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isBlank()) continue;

            String[] cols = line.split(",", -1);
            if (cols.length < 4) {
                failures.add("Row " + (i + 1) + " — invalid format (need: pincode,city,state,zone_code[,serviceable])");
                continue;
            }

            String pin      = cols[0].trim();
            String city     = cols[1].trim();
            String stateVal = cols[2].trim();
            String zoneCode = cols[3].trim().toUpperCase();
            boolean serviceable = cols.length < 5 || !"false".equalsIgnoreCase(cols[4].trim());

            if (!pin.matches("\\d{6}")) {
                failures.add("Row " + (i + 1) + " — invalid PIN code: " + pin);
                continue;
            }

            Optional<DeliveryZone> zoneOpt = zoneRepo.findByCode(zoneCode);
            if (zoneOpt.isEmpty()) {
                failures.add("Row " + (i + 1) + " — zone not found: " + zoneCode);
                continue;
            }

            Pincode p = repo.findById(pin).orElseGet(Pincode::new);
            p.setPincode(pin);
            p.setCity(city.isBlank() ? null : city);
            p.setState(stateVal.isBlank() ? null : stateVal);
            p.setZone(zoneOpt.get());
            p.setServiceable(serviceable);
            repo.save(p);
            imported++;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalRows",  lines.length - 1);
        result.put("imported",   imported);
        result.put("failed",     failures.size());
        result.put("failures",   failures);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    private void resolveZone(Pincode p, Map<String, Object> fields) {
        if (!fields.containsKey("zoneId")) return;
        Object val = fields.get("zoneId");
        if (val == null) {
            p.setZone(null);
        } else {
            UUID zoneId = UUID.fromString(val.toString());
            DeliveryZone zone = zoneRepo.findById(zoneId)
                .orElseThrow(() -> new EntityNotFoundException("DeliveryZone not found: " + zoneId));
            p.setZone(zone);
        }
    }
}
