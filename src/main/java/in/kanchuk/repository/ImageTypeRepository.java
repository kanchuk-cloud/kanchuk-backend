package in.kanchuk.repository;

import in.kanchuk.entity.ImageType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ImageTypeRepository extends JpaRepository<ImageType, UUID> {
    List<ImageType> findAllByOrderBySortOrderAsc();
    List<ImageType> findAllByIsActiveTrueOrderBySortOrderAsc();
    boolean existsByValue(String value);
}
