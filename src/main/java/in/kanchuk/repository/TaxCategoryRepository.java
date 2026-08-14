package in.kanchuk.repository;

import in.kanchuk.entity.TaxCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TaxCategoryRepository extends JpaRepository<TaxCategory, UUID> {
    Page<TaxCategory> findAll(Pageable pageable);
    Page<TaxCategory> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
