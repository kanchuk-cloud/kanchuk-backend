package in.kanchuk.controller.admin;

import in.kanchuk.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/tax")
public class AdminTaxController {

    @GetMapping("/gst-rates")
    public ResponseEntity<ApiResponse<Map<String, Object>>> gstRates() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("tiers", List.of(
            Map.of("label", "Standard Rate", "upToMrp", 2500, "rate", 5,  "rateDisplay", "5%",
                   "cgst", "2.5%", "sgst", "2.5%", "igst", "5%"),
            Map.of("label", "Premium Rate",  "aboveMrp", 2500, "rate", 18, "rateDisplay", "18%",
                   "cgst", "9%",   "sgst", "9%",   "igst", "18%")
        ));
        result.put("note", "Rates apply per piece MRP. GST is inclusive in all displayed prices.");
        result.put("thresholdMrp", 2500);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
