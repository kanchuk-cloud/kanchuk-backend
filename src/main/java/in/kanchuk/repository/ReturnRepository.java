package in.kanchuk.repository;

import in.kanchuk.entity.Return;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReturnRepository extends JpaRepository<Return, UUID> {
    Page<Return> findAll(Pageable pageable);
}
