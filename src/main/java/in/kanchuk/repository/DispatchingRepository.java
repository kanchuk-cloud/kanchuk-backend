package in.kanchuk.repository;

import in.kanchuk.entity.Dispatching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DispatchingRepository extends JpaRepository<Dispatching, UUID> {
    Page<Dispatching> findAll(Pageable pageable);
    Optional<Dispatching> findByOrderId(UUID orderId);
}
