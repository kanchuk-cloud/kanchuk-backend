package in.kanchuk.repository;

import in.kanchuk.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Page<Category> findByDeletedAtIsNull(Pageable pageable);
    Page<Category> findByDeletedAtIsNullAndNameContainingIgnoreCase(String name, Pageable pageable);
    List<Category> findByDeletedAtIsNullAndIsActiveTrue();
    Optional<Category> findBySlugAndDeletedAtIsNull(String slug);
}
