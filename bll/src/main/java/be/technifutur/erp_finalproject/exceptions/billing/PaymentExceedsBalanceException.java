package be.technifutur.erp_finalproject.exceptions.billing;

import be.technifutur.erp_finalproject.exceptions.ConflictException;

import java.math.BigDecimal;

public class PaymentExceedsBalanceException extends ConflictException {

    private final Long billingId;
    private final BigDecimal remaining;
    private final BigDecimal attempted;

    public PaymentExceedsBalanceException(Long billingId, BigDecimal remaining, BigDecimal attempted) {
        super("Versement de " + attempted + " € refusé : il reste " + remaining
                + " € à payer sur la facture " + billingId);
        this.billingId = billingId;
        this.remaining = remaining;
        this.attempted = attempted;
    }
}
