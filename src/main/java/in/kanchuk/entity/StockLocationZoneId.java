package in.kanchuk.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class StockLocationZoneId implements Serializable {

    @Column(name = "location_id")
    private UUID locationId;

    @Column(name = "zone_id")
    private UUID zoneId;
}
