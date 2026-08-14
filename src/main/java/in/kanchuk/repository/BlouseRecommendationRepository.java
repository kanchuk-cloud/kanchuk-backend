package in.kanchuk.repository;

import in.kanchuk.entity.BlouseRecommendation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BlouseRecommendationRepository extends JpaRepository<BlouseRecommendation, UUID> {
    Page<BlouseRecommendation> findAll(Pageable pageable);
    Page<BlouseRecommendation> findByStyleNameContainingIgnoreCase(String styleName, Pageable pageable);
    List<BlouseRecommendation> findTop4ByIsActiveTrueAndAppliesFabricIsNullOrAppliesFabricOrderBySortOrderAsc(String fabric);
}
