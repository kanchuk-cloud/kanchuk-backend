package in.kanchuk.repository;

import in.kanchuk.entity.SizeChart;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SizeChartRepository extends JpaRepository<SizeChart, UUID> {
    @EntityGraph(attributePaths = {"category"})
    Page<SizeChart> findByDeletedAtIsNull(Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    Page<SizeChart> findByDeletedAtIsNullAndNameContainingIgnoreCase(String name, Pageable pageable);

    List<SizeChart> findByCategory_SlugAndDeletedAtIsNullAndIsActiveTrue(String categorySlug);
}
