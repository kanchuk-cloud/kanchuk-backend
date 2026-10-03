package in.kanchuk.repository;

import in.kanchuk.entity.MidBanner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MidBannerRepository extends JpaRepository<MidBanner, UUID> {
    Page<MidBanner> findAll(Pageable pageable);
    List<MidBanner> findByIsActiveTrueOrderBySortOrderAsc();
}
