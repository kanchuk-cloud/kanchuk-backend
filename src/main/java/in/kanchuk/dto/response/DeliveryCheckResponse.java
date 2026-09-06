package in.kanchuk.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeliveryCheckResponse {

    private boolean serviceable;
    private String pincode;
    private String city;
    private String state;
    private String message;
    private ZoneInfo zone;
    private DeliveryOption standardDelivery;
    private DeliveryOption expressDelivery;
    private Boolean codAvailable;
    private String hubName;
    private String hubCity;
    private Boolean isFallbackHub;

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ZoneInfo {
        private String code;
        private String name;
    }

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DeliveryOption {
        private boolean available;
        private BigDecimal charge;
        private Integer estimatedDays;
        private Integer estimatedMinutes;
    }
}
