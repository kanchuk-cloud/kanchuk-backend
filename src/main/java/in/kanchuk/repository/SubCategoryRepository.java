package in.kanchuk.repository;

import in.kanchuk.entity.SubCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SubCategoryRepository extends JpaRepository<SubCategory, UUID> {
    @EntityGraph(attributePaths = {"category"})
    Page<SubCategory> findByDeletedAtIsNull(Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    Page<SubCategory> findByDeletedAtIsNullAndNameContainingIgnoreCase(String name, Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    Page<SubCategory> findByDeletedAtIsNullAndCategory_Id(UUID categoryId, Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    Page<SubCategory> findByDeletedAtIsNullAndCategory_IdAndNameContainingIgnoreCase(UUID categoryId, String name, Pageable pageable);

    List<SubCategory> findByCategory_IdAndDeletedAtIsNull(UUID categoryId);
}
