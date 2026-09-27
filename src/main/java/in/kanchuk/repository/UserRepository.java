package in.kanchuk.repository;

import in.kanchuk.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);

    Page<User> findByDeletedAtIsNull(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL AND " +
           "(LOWER(u.name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           " LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           " u.phone LIKE CONCAT('%', :q, '%'))")
    Page<User> searchCustomers(@Param("q") String q, Pageable pageable);
}
