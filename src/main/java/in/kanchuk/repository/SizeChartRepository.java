package in.kanchuk.repository;

import in.kanchuk.entity.SizeChart;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SizeChartRepository extends JpaRepository<SizeChart, UUID> {
    Page<SizeChart> findByDeletedAtIsNull(Pageable pageable);
    Page<SizeChart> findByDeletedAtIsNullAndNameContainingIgnoreCase(String name, Pageable pageable);
}
