package in.kanchuk.repository;

import in.kanchuk.entity.WalletLedger;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface WalletLedgerRepository extends JpaRepository<WalletLedger, UUID> {

    @Query("SELECT wl FROM WalletLedger wl WHERE wl.user.id = :userId ORDER BY wl.createdAt DESC")
    List<WalletLedger> findRecentByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT wl FROM WalletLedger wl WHERE wl.type = 'COIN_EARN' " +
           "AND wl.expiresAt <= :today AND wl.isPending = false " +
           "AND NOT EXISTS (SELECT 1 FROM WalletLedger e WHERE e.user = wl.user " +
           "  AND e.type = 'COIN_EXPIRE' AND e.order = wl.order AND e.createdAt > wl.createdAt)")
    List<WalletLedger> findExpiredCoinEarns(@Param("today") LocalDate today);

    boolean existsByOrder_IdAndType(UUID orderId, String type);

    List<WalletLedger> findByOrder_IdAndType(UUID orderId, String type);

    @Query("SELECT COALESCE(SUM(wl.coinAmount), 0) FROM WalletLedger wl " +
           "WHERE wl.user.id = :userId AND wl.type = 'COIN_EARN' " +
           "AND wl.isPending = false AND wl.expiresAt > :cutoff")
    Integer sumActiveCoinEarns(@Param("userId") UUID userId, @Param("cutoff") LocalDate cutoff);
}
