package be.technifutur.erp_finalproject.services.purchaseorderlineservice;

public record PurchaseOrderLineForm(
        Long productId,
        int quantity
) {
}
