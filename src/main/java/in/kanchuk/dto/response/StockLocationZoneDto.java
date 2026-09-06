package in.kanchuk.dto.response;

import in.kanchuk.entity.DeliveryZone;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class StockLocationZoneDto {
    private UUID locationId;
    private UUID zoneId;
    private int priority;
    private DeliveryZone zone;
}
