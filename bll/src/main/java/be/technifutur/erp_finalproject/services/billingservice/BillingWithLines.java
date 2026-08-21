package be.technifutur.erp_finalproject.services.billingservice;

import be.technifutur.erp_finalproject.entities.Billing;
import be.technifutur.erp_finalproject.entities.BillingLine;

import java.math.BigDecimal;
import java.util.List;

public record BillingWithLines(
        Billing billing,
        List<BillingLine> lines,
        BigDecimal paidAmount
) {
}
