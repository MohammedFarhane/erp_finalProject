package be.technifutur.erp_finalproject.models.dto_request;

import be.technifutur.erp_finalproject.services.billingservice.BillingForm;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record BillingRequest(
        @NotNull Long clientId,
        @NotNull Long userId,
        BigDecimal discount,
        @NotEmpty @Valid List<BillingLineRequest> lines
) {
    public BillingForm toForm() {
        return new BillingForm(
                clientId,
                userId,
                discount,
                lines.stream().map(BillingLineRequest::toForm).toList()
        );
    }
}
