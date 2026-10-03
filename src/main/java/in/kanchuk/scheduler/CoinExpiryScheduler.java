package in.kanchuk.scheduler;

import in.kanchuk.entity.WalletLedger;
import in.kanchuk.repository.UserRepository;
import in.kanchuk.repository.WalletLedgerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CoinExpiryScheduler {

    private final WalletLedgerRepository ledgerRepo;
    private final UserRepository userRepo;

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void expireCoins() {
        List<WalletLedger> expired = ledgerRepo.findExpiredCoinEarns(LocalDate.now());
        if (expired.isEmpty()) return;

        // Group by user
        Map<UUID, List<WalletLedger>> byUser = expired.stream()
                .collect(Collectors.groupingBy(e -> e.getUser().getId()));

        byUser.forEach((userId, entries) -> {
            userRepo.findById(userId).ifPresent(user -> {
                int totalExpiring = entries.stream()
                        .mapToInt(e -> e.getCoinAmount() != null ? Math.max(0, e.getCoinAmount()) : 0)
                        .sum();
                if (totalExpiring == 0) return;

                int actualExpiry = Math.min(totalExpiring, user.getLoyaltyPoints());
                user.setLoyaltyPoints(Math.max(0, user.getLoyaltyPoints() - actualExpiry));

                WalletLedger expireEntry = new WalletLedger();
                expireEntry.setUser(user);
                expireEntry.setType("COIN_EXPIRE");
                expireEntry.setCoinAmount(-actualExpiry);
                expireEntry.setCoinBalanceAfter(user.getLoyaltyPoints());
                expireEntry.setNote("Coins expired on " + LocalDate.now());

                userRepo.save(user);
                ledgerRepo.save(expireEntry);
                log.info("Expired {} coins for user {}", actualExpiry, userId);
            });
        });
    }
}
