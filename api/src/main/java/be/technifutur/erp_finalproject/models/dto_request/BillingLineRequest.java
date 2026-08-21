package be.technifutur.erp_finalproject.models.dto_request;

import be.technifutur.erp_finalproject.services.billinglineservice.BillingLineForm;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BillingLineRequest(
        @NotNull Long productId,
        @Positive int quantity
) {
    public BillingLineForm toForm (){
        return new BillingLineForm(
                productId,
                quantity
        );
    }
}
