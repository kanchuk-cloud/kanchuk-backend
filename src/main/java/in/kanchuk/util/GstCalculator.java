package in.kanchuk.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class GstCalculator {

    public static final BigDecimal THRESHOLD = new BigDecimal("2500");
    public static final BigDecimal RATE_LOW  = new BigDecimal("0.05");
    public static final BigDecimal RATE_HIGH = new BigDecimal("0.18");

    public record TaxBreakdown(
            BigDecimal taxableValue,
            BigDecimal gstRate,
            BigDecimal taxAmount,
            BigDecimal cgst,
            BigDecimal sgst,
            BigDecimal igst,
            boolean interState
    ) {}

    public static TaxBreakdown calculate(BigDecimal unitPrice, boolean interState) {
        BigDecimal rate = unitPrice.compareTo(THRESHOLD) <= 0 ? RATE_LOW : RATE_HIGH;
        BigDecimal divisor = BigDecimal.ONE.add(rate);
        BigDecimal taxable = unitPrice.divide(divisor, 2, RoundingMode.HALF_UP);
        BigDecimal tax = unitPrice.subtract(taxable).setScale(2, RoundingMode.HALF_UP);
        BigDecimal half = tax.divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
        return new TaxBreakdown(
                taxable,
                rate,
                tax,
                interState ? BigDecimal.ZERO : half,
                interState ? BigDecimal.ZERO : half,
                interState ? tax : BigDecimal.ZERO,
                interState
        );
    }
}
