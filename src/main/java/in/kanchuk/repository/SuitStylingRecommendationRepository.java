package in.kanchuk.repository;

import in.kanchuk.entity.SuitStylingRecommendation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SuitStylingRecommendationRepository extends JpaRepository<SuitStylingRecommendation, UUID> {
    Page<SuitStylingRecommendation> findAll(Pageable pageable);
    Page<SuitStylingRecommendation> findByStyleNameContainingIgnoreCase(String styleName, Pageable pageable);
}
