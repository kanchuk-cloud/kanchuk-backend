package in.kanchuk.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "loyalty_settings")
public class LoyaltySettings {

    @Id
    private Long id;

    @Column(name = "coins_earn_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal coinsEarnPct = new BigDecimal("2.00");

    @Column(name = "coins_redeem_max_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal coinsRedeemMaxPct = new BigDecimal("10.00");

    @Column(name = "coins_per_rupee", nullable = false, precision = 5, scale = 2)
    private BigDecimal coinsPerRupee = new BigDecimal("1.00");

    @Column(name = "coins_expiry_days", nullable = false)
    private int coinsExpiryDays = 365;

    @Column(name = "wallet_expiry_days", nullable = false)
    private int walletExpiryDays = 730;

    @Column(name = "platform_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal platformFee = new BigDecimal("23.00");

    @Column(name = "platform_fee_enabled", nullable = false)
    private boolean platformFeeEnabled = true;

    @Column(name = "min_order_for_coins", nullable = false, precision = 10, scale = 2)
    private BigDecimal minOrderForCoins = BigDecimal.ZERO;

    @Column(name = "coins_on_coin_payment", nullable = false)
    private boolean coinsOnCoinPayment = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
