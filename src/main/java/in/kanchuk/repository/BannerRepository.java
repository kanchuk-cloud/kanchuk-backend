package in.kanchuk.repository;

import in.kanchuk.entity.Banner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BannerRepository extends JpaRepository<Banner, UUID> {
    Page<Banner> findByDeletedAtIsNull(Pageable pageable);
    Page<Banner> findByDeletedAtIsNullAndTitleContainingIgnoreCase(String title, Pageable pageable);
}
