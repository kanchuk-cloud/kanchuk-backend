package in.kanchuk.repository;

import in.kanchuk.entity.Designer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DesignerRepository extends JpaRepository<Designer, UUID> {
    Page<Designer> findByDeletedAtIsNull(Pageable pageable);
    Page<Designer> findByDeletedAtIsNullAndNameContainingIgnoreCase(String name, Pageable pageable);
    List<Designer> findByDeletedAtIsNullAndIsActiveTrueOrderBySortOrderAsc();
}
