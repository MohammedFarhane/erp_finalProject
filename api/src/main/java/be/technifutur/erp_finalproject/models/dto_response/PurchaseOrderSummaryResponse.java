package be.technifutur.erp_finalproject.models.dto_response;

import be.technifutur.erp_finalproject.entities.PurchaseOrder;
import be.technifutur.erp_finalproject.enums.PurchaseOrderState;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PurchaseOrderSummaryResponse(
        Long id,
        String reference,
        LocalDate date,
        PurchaseOrderState state,
        BigDecimal totalPrice,
        String supplierName
) {
    public static PurchaseOrderSummaryResponse from(PurchaseOrder order) {
        return new PurchaseOrderSummaryResponse(
                order.getId(),
                order.getReference(),
                order.getDate(),
                order.getState(),
                order.getTotalPrice(),
                order.getSupplier().getName());
    }
}