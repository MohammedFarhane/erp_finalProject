package be.technifutur.erp_finalproject.models.dto_response;

import be.technifutur.erp_finalproject.entities.Billing;
import be.technifutur.erp_finalproject.enums.BillingState;
import be.technifutur.erp_finalproject.services.billingservice.BillingSummary;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public record BillingSummaryResponse(
        Long billingId,
        String reference,
        LocalDate billingDate,
        BillingState state,
        BigDecimal totalPrice,
        BigDecimal paidAmount,
        BigDecimal remainingAmount,
        String clientName

) {
    public static BillingSummaryResponse from(BillingSummary summary) {
        Billing billing = summary.billing();
        BigDecimal paid = summary.paidAmount().setScale(2, RoundingMode.HALF_UP);

        return new BillingSummaryResponse(
                billing.getId(),
                billing.getReference(),
                billing.getBillingDate(),
                billing.getState(),
                billing.getTotalPrice(),
                paid,
                billing.getTotalPrice().subtract(summary.paidAmount()),
                billing.getClient().getName()
        );
    }
}
