package in.kanchuk.repository;

import in.kanchuk.entity.Packaging;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PackagingRepository extends JpaRepository<Packaging, UUID> {
    Page<Packaging> findAll(Pageable pageable);
    Optional<Packaging> findByOrderId(UUID orderId);
}
