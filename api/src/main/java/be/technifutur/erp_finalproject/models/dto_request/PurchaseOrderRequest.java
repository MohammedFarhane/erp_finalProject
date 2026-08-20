package be.technifutur.erp_finalproject.models.dto_request;

import be.technifutur.erp_finalproject.services.purchaseorderservice.PurchaseOrderForm;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PurchaseOrderRequest(
        @NotNull Long supplierId,
        @NotNull Long userId,
        @NotEmpty @Valid List<PurchaseOrderLineRequest> lines
) {
    public PurchaseOrderForm toForm() {
        return new PurchaseOrderForm(
                supplierId,
                userId,
                lines.stream().map(PurchaseOrderLineRequest::toForm).toList()
        );
    }
}
