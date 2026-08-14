package in.kanchuk.repository;

import in.kanchuk.entity.Facet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FacetRepository extends JpaRepository<Facet, UUID> {
    Page<Facet> findByDeletedAtIsNull(Pageable pageable);
    Page<Facet> findByDeletedAtIsNullAndNameContainingIgnoreCase(String name, Pageable pageable);
}
