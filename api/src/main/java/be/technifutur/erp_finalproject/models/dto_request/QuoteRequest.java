package be.technifutur.erp_finalproject.models.dto_request;

import be.technifutur.erp_finalproject.services.quoteservice.QuoteForm;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record QuoteRequest(
        @NotNull Long clientId,
        @NotNull Long userId,
        @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal discount,
        @NotEmpty @Valid List<QuoteLineRequest> lines
) {
    public QuoteForm toForm() {
        return new QuoteForm(
                clientId,
                userId,
                discount,
                lines.stream().map(QuoteLineRequest::toForm).toList()
        );
    }
}