package be.technifutur.erp_finalproject.models.dto_response;

import be.technifutur.erp_finalproject.entities.Billing;
import be.technifutur.erp_finalproject.enums.BillingState;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BillingSummaryResponse(
        Long billingId,
        String reference,
        LocalDate billingDate,
        BillingState state,
        BigDecimal totalPrice,
        String clientName

) {
    public static BillingSummaryResponse from(Billing billing) {
        return new BillingSummaryResponse(
                billing.getId(),
                billing.getReference(),
                billing.getBillingDate(),
                billing.getState(),
                billing.getTotalPrice(),
                billing.getClient().getName()
        );
    }
}
