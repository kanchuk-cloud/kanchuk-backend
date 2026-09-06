package in.kanchuk.repository;

import in.kanchuk.entity.StockLocationZone;
import in.kanchuk.entity.StockLocationZoneId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface StockLocationZoneRepository extends JpaRepository<StockLocationZone, StockLocationZoneId> {

    @Query("SELECT slz FROM StockLocationZone slz JOIN FETCH slz.location WHERE slz.id.zoneId = :zoneId ORDER BY slz.priority ASC")
    List<StockLocationZone> findByZoneIdOrderByPriority(@Param("zoneId") UUID zoneId);

    @Query("SELECT slz FROM StockLocationZone slz WHERE slz.id.locationId = :locationId ORDER BY slz.priority ASC")
    List<StockLocationZone> findByLocationIdOrderByPriority(@Param("locationId") UUID locationId);

    @Modifying
    @Transactional
    @Query("DELETE FROM StockLocationZone slz WHERE slz.id.locationId = :locationId AND slz.id.zoneId = :zoneId")
    void deleteByLocationIdAndZoneId(@Param("locationId") UUID locationId, @Param("zoneId") UUID zoneId);
}
