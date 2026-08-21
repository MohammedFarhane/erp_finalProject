package be.technifutur.erp_finalproject.services.billingservice;

import be.technifutur.erp_finalproject.services.billinglineservice.BillingLineForm;

import java.math.BigDecimal;
import java.util.List;

public record BillingForm(
        Long clientId,
        Long userId,
        BigDecimal discount,
        List<BillingLineForm> lines
) {
}
