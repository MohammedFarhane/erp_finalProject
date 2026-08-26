package be.technifutur.erp_finalproject.models.dto_request;

import be.technifutur.erp_finalproject.services.billingservice.BillingForm;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record BillingRequest(

        @NotNull
        Long clientId,

        @DecimalMin("0.00") @DecimalMax("100.00")
        BigDecimal discount,

        @NotEmpty @Valid
        List<BillingLineRequest> lines
) {
    public BillingForm toForm(Long userId) {
        return new BillingForm(
                clientId,
                userId,
                discount,
                lines.stream().map(BillingLineRequest::toForm).toList()
        );
    }
}
