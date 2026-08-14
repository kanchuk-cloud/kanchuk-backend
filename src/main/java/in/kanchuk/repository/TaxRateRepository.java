package in.kanchuk.repository;

import in.kanchuk.entity.TaxRate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaxRateRepository extends JpaRepository<TaxRate, UUID> {
    Page<TaxRate> findAll(Pageable pageable);
    List<TaxRate> findByTaxCategoryId(UUID taxCategoryId);
}
