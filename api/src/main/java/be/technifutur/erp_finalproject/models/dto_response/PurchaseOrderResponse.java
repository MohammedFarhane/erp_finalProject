package be.technifutur.erp_finalproject.models.dto_response;

import be.technifutur.erp_finalproject.entities.PurchaseOrder;
import be.technifutur.erp_finalproject.enums.PurchaseOrderState;
import be.technifutur.erp_finalproject.services.purchaseorderservice.PurchaseOrderWithLines;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PurchaseOrderResponse(
        Long id,
        String reference,
        LocalDate date,
        PurchaseOrderState state,
        BigDecimal totalPrice,
        SupplierResponse supplier,
        List<PurchaseOrderLineResponse> lines
) {
    public static PurchaseOrderResponse from(PurchaseOrderWithLines pwl) {
        PurchaseOrder order = pwl.order();
        return new PurchaseOrderResponse(
            order.getId(),
            order.getReference(),
            order.getDate(),
            order.getState(),
            order.getTotalPrice(),
            SupplierResponse.from(order.getSupplier()),
            pwl.lines()
                    .stream()
                    .map(PurchaseOrderLineResponse::from)
                    .toList()
        );
    }
}