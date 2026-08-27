package be.technifutur.erp_finalproject.services.purchaseorderservice;

public record PurchaseOrderLineForm(
        Long productId,
        int quantity
) {
}
