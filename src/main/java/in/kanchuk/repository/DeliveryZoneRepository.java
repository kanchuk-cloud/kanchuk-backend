package in.kanchuk.repository;

import in.kanchuk.entity.DeliveryZone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DeliveryZoneRepository extends JpaRepository<DeliveryZone, UUID> {
    Page<DeliveryZone> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Optional<DeliveryZone> findByCode(String code);
}
