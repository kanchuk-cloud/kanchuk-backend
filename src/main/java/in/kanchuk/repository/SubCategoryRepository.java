package in.kanchuk.repository;

import in.kanchuk.entity.SubCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SubCategoryRepository extends JpaRepository<SubCategory, UUID> {
    Page<SubCategory> findByDeletedAtIsNull(Pageable pageable);
    Page<SubCategory> findByDeletedAtIsNullAndNameContainingIgnoreCase(String name, Pageable pageable);
    List<SubCategory> findByCategoryIdAndDeletedAtIsNull(UUID categoryId);
}
