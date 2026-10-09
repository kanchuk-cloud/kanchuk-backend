package in.kanchuk.controller.admin;

import in.kanchuk.dto.response.ApiResponse;
import in.kanchuk.entity.Seller;
import in.kanchuk.entity.VendorDocument;
import in.kanchuk.entity.VendorStatusHistory;
import in.kanchuk.repository.AdminUserRepository;
import in.kanchuk.repository.SellerRepository;
import in.kanchuk.repository.VendorDocumentRepository;
import in.kanchuk.repository.VendorStatusHistoryRepository;
import in.kanchuk.service.GenericAdminService;
import in.kanchuk.service.VendorService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/vendors")
@RequiredArgsConstructor
public class AdminVendorOnboardingController extends GenericAdminService {

    private final SellerRepository sellerRepo;
    private final VendorDocumentRepository docRepo;
    private final VendorStatusHistoryRepository historyRepo;
    private final AdminUserRepository adminUserRepo;
    private final VendorService vendorService;

    // ── Vendor CRUD ──────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<List<Seller>>> list(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false)  String status,
            @RequestParam(required = false)  String kycStatus,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "20") int limit) {

        String searchVal    = search.isBlank() ? null : search;
        String statusVal    = (status    != null && !status.isBlank())    ? status    : null;
        String kycStatusVal = (kycStatus != null && !kycStatus.isBlank()) ? kycStatus : null;

        Page<Seller> pg = sellerRepo.findFiltered(searchVal, statusVal, kycStatusVal, pageRequest(page, limit));
        return ResponseEntity.ok(ApiResponse.ok(pg.getContent(), buildMeta(pg, page, limit)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Seller>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(findOrThrow(sellerRepo, id, "Vendor")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Seller>> create(@RequestBody Seller body) {
        if (body.isMsme() && (body.getUdyamNumber() == null || body.getUdyamNumber().isBlank())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("udyam_number is required when is_msme = true"));
        }
        body.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(sellerRepo.save(body)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Seller>> update(@PathVariable UUID id, @RequestBody Map<String, Object> fields) {
        Seller e = findOrThrow(sellerRepo, id, "Vendor");
        // Validate MSME constraint
        Object isMsme    = fields.get("isMsme");
        Object udyamNum  = fields.get("udyamNumber");
        boolean willBeMsme = isMsme != null ? Boolean.parseBoolean(isMsme.toString()) : e.isMsme();
        String udyam = udyamNum != null ? udyamNum.toString() : e.getUdyamNumber();
        if (willBeMsme && (udyam == null || udyam.isBlank())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("udyam_number is required when is_msme = true"));
        }
        applyPatch(e, fields);
        return ResponseEntity.ok(ApiResponse.ok(sellerRepo.save(e)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        Seller e = findOrThrow(sellerRepo, id, "Vendor");
        e.softDelete();
        sellerRepo.save(e);
        return ResponseEntity.ok(ApiResponse.deleted());
    }

    // ── Status transitions ───────────────────────────────────────────────────

    @PostMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Seller>> updateStatus(
            @PathVariable UUID id, @RequestBody Map<String, Object> body) {
        String newStatus = (String) body.get("newStatus");
        String reason    = (String) body.get("reason");
        UUID   adminId   = body.get("adminId") != null ? UUID.fromString(body.get("adminId").toString()) : null;
        try {
            Seller s = vendorService.updateStatus(id, newStatus, reason, adminId);
            return ResponseEntity.ok(ApiResponse.ok(s));
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
        }
    }

    @PostMapping("/{id}/kyc-status")
    public ResponseEntity<ApiResponse<Seller>> updateKycStatus(
            @PathVariable UUID id, @RequestBody Map<String, Object> body) {
        String newStatus = (String) body.get("newStatus");
        String reason    = (String) body.get("reason");
        UUID   adminId   = body.get("adminId") != null ? UUID.fromString(body.get("adminId").toString()) : null;
        Seller s = vendorService.updateKycStatus(id, newStatus, reason, adminId);
        return ResponseEntity.ok(ApiResponse.ok(s));
    }

    @GetMapping("/{id}/status-history")
    public ResponseEntity<ApiResponse<List<VendorStatusHistory>>> statusHistory(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(historyRepo.findByVendorIdOrderByChangedAtDesc(id)));
    }

    // ── Documents ────────────────────────────────────────────────────────────

    @GetMapping("/{id}/documents")
    public ResponseEntity<ApiResponse<List<VendorDocument>>> listDocuments(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(docRepo.findByVendorId(id)));
    }

    @PostMapping("/{id}/documents")
    public ResponseEntity<ApiResponse<VendorDocument>> uploadDocument(
            @PathVariable UUID id, @RequestBody Map<String, Object> body) {
        Seller seller = findOrThrow(sellerRepo, id, "Vendor");
        VendorDocument doc = new VendorDocument();
        doc.setVendor(seller);
        doc.setDocumentType((String) body.get("documentType"));
        doc.setFileUrl((String) body.get("fileUrl"));
        if (body.get("uploadedBy") != null) doc.setUploadedBy(body.get("uploadedBy").toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(docRepo.save(doc)));
    }

    @PatchMapping("/{id}/documents/{docId}")
    public ResponseEntity<ApiResponse<VendorDocument>> verifyDocument(
            @PathVariable UUID id,
            @PathVariable UUID docId,
            @RequestBody Map<String, Object> body) {

        VendorDocument doc = docRepo.findById(docId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found: " + docId));

        String newStatus = (String) body.get("verificationStatus");
        if (newStatus != null) doc.setVerificationStatus(newStatus);
        if (body.get("rejectionReason") != null) doc.setRejectionReason(body.get("rejectionReason").toString());

        if ("verified".equals(newStatus)) {
            doc.setVerifiedAt(OffsetDateTime.now());
            UUID adminId = body.get("verifiedBy") != null ? UUID.fromString(body.get("verifiedBy").toString()) : null;
            if (adminId != null) adminUserRepo.findById(adminId).ifPresent(doc::setVerifiedBy);
            docRepo.save(doc);
            vendorService.checkAndAutoVerifyKyc(id);
        } else {
            docRepo.save(doc);
        }
        return ResponseEntity.ok(ApiResponse.ok(doc));
    }

    // ── Activation check ─────────────────────────────────────────────────────

    @GetMapping("/{id}/activation-check")
    public ResponseEntity<ApiResponse<Map<String, Object>>> activationCheck(@PathVariable UUID id) {
        List<String> missing = vendorService.getMissingVerifiedDocuments(id);
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "eligible",    missing.isEmpty(),
                "missingDocs", missing)));
    }
}
