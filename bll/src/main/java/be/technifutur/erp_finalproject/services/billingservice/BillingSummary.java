package be.technifutur.erp_finalproject.services.billingservice;

import be.technifutur.erp_finalproject.entities.Billing;

import java.math.BigDecimal;

public record BillingSummary(
        Billing billing,
        BigDecimal paidAmount
) {
}
