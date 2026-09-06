package in.kanchuk.repository;

import in.kanchuk.entity.MediaAsset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, UUID> {
    Page<MediaAsset> findByCategory(String category, Pageable pageable);
    Page<MediaAsset> findByCategoryAndSubcategory(String category, String subcategory, Pageable pageable);
}
