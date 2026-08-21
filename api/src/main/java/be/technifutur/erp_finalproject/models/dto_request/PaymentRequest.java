package be.technifutur.erp_finalproject.models.dto_request;

import be.technifutur.erp_finalproject.enums.PaymentMethod;
import be.technifutur.erp_finalproject.services.billingservice.PaymentForm;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PaymentRequest(
        @NotNull @Positive BigDecimal amount,
        @NotNull PaymentMethod method,
        @NotNull Long userId
) {
    public PaymentForm toForm() {
        return new PaymentForm(
                amount,
                method,
                userId
        );
    }
}
