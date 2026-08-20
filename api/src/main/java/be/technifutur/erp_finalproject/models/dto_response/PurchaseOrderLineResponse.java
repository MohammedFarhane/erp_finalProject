package be.technifutur.erp_finalproject.models.dto_response;

import be.technifutur.erp_finalproject.entities.PurchaseOrderLine;

import java.math.BigDecimal;

public record PurchaseOrderLineResponse(
        String productName,
        int quantity,
        BigDecimal unitPrice
) {
    public static PurchaseOrderLineResponse from(PurchaseOrderLine purchaseOrderLine) {
        return new PurchaseOrderLineResponse(
                purchaseOrderLine.getProduct().getName(),
                purchaseOrderLine.getQuantity(),
                purchaseOrderLine.getUnitPrice()
        );
    }
}
