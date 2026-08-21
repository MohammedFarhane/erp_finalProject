package be.technifutur.erp_finalproject.services.billingservice;

import be.technifutur.erp_finalproject.enums.PaymentMethod;

import java.math.BigDecimal;

public record PaymentForm(
        BigDecimal amount,
        PaymentMethod method,
        Long userId
) {
}
