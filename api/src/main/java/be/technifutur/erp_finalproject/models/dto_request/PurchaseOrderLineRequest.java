package be.technifutur.erp_finalproject.models.dto_request;

import be.technifutur.erp_finalproject.services.purchaseorderservice.PurchaseOrderLineForm;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PurchaseOrderLineRequest(
        @NotNull Long productId,
        @Positive int quantity
) {
    public PurchaseOrderLineForm toForm() {
        return new PurchaseOrderLineForm(
                productId,
                quantity
        );
    }
}