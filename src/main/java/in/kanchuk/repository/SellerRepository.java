package in.kanchuk.repository;

import in.kanchuk.entity.Seller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface SellerRepository extends JpaRepository<Seller, UUID> {
    Page<Seller> findByDeletedAtIsNull(Pageable pageable);
    Page<Seller> findByDeletedAtIsNullAndNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("""
        SELECT s FROM Seller s
        WHERE s.deletedAt IS NULL
          AND (COALESCE(:search, '') = '' OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))
                                         OR LOWER(s.email) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:status    IS NULL OR s.status    = :status)
          AND (:kycStatus IS NULL OR s.kycStatus = :kycStatus)
        ORDER BY s.createdAt DESC
        """)
    Page<Seller> findFiltered(
            @Param("search")    String search,
            @Param("status")    String status,
            @Param("kycStatus") String kycStatus,
            Pageable pageable);
}
