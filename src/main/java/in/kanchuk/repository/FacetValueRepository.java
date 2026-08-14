package in.kanchuk.repository;

import in.kanchuk.entity.FacetValue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FacetValueRepository extends JpaRepository<FacetValue, UUID> {
    Page<FacetValue> findByDeletedAtIsNull(Pageable pageable);
    List<FacetValue> findByFacetIdAndDeletedAtIsNull(UUID facetId);
}
