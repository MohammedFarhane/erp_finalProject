package be.technifutur.erp_finalproject.models.dto_request;

import be.technifutur.erp_finalproject.services.quoteservice.QuoteLineForm;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record QuoteLineRequest(
        @NotNull Long productId,
        @Positive int quantity
) {
    public QuoteLineForm toForm(){
        return new QuoteLineForm(
                productId,
                quantity
        );
    }

}