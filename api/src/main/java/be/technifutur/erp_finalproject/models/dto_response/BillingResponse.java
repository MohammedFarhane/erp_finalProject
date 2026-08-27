package be.technifutur.erp_finalproject.models.dto_response;

import be.technifutur.erp_finalproject.entities.Billing;
import be.technifutur.erp_finalproject.enums.BillingState;
import be.technifutur.erp_finalproject.services.billingservice.BillingWithLines;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

public record BillingResponse(
        Long billingId,
        String reference,
        LocalDate billingDate,
        BillingState state,
        BigDecimal subTotal,
        BigDecimal discount,
        BigDecimal amountTva,
        BigDecimal totalPrice,
        BigDecimal paidAmount,
        BigDecimal remainingAmount,
        String clientName,
        List<BillingLineResponse> lines
) {
    public static BillingResponse from(BillingWithLines bwl) {
        Billing billing = bwl.billing();
        BigDecimal paid = bwl.paidAmount().setScale(2, RoundingMode.HALF_UP);
        BigDecimal remaining = billing.getTotalPrice().subtract(paid);

        return new BillingResponse(
                billing.getId(),
                billing.getReference(),
                billing.getBillingDate(),
                billing.getState(),
                billing.getSubTotal(),
                billing.getDiscount(),
                billing.getAmountTva(),
                billing.getTotalPrice(),
                paid,
                remaining,
                billing.getClient().getName(),
                bwl.lines().stream().map(BillingLineResponse::from).toList()
        );
    }
}
