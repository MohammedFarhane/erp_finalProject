package be.technifutur.erp_finalproject;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Totals(
        BigDecimal subTotal,
        BigDecimal amountTva,
        BigDecimal totalPrice
) {
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    public static Totals of(BigDecimal subTotal, BigDecimal tvaBrute, BigDecimal discount) {

        BigDecimal coeff = BigDecimal.ONE;

        if (discount != null && discount.compareTo(BigDecimal.ZERO) > 0) {
            coeff = BigDecimal.ONE.subtract(discount.divide(HUNDRED, 4, RoundingMode.HALF_UP));
        }

        BigDecimal amountTva = tvaBrute
                .multiply(coeff)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalPrice = subTotal
                .multiply(coeff)
                .add(amountTva)
                .setScale(2, RoundingMode.HALF_UP);

        return new Totals(subTotal, amountTva, totalPrice);
    }
}
