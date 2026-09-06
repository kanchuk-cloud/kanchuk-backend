package in.kanchuk.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "stock_location_zones")
public class StockLocationZone {

    @EmbeddedId
    private StockLocationZoneId id = new StockLocationZoneId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("locationId")
    @JoinColumn(name = "location_id")
    private StockLocation location;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("zoneId")
    @JoinColumn(name = "zone_id")
    private DeliveryZone zone;

    @Column(nullable = false)
    private int priority = 1;
}
