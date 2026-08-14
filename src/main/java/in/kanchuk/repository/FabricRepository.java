package in.kanchuk.repository;

import in.kanchuk.entity.Fabric;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FabricRepository extends JpaRepository<Fabric, UUID> {
    Page<Fabric> findByDeletedAtIsNullAndNameContainingIgnoreCase(String search, Pageable pageable);
    Page<Fabric> findByDeletedAtIsNull(Pageable pageable);
}
