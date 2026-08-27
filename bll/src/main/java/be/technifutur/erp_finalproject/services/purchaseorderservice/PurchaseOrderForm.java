package be.technifutur.erp_finalproject.services.purchaseorderservice;

import java.util.List;

public record PurchaseOrderForm(
    Long supplierId,
    Long userId,
    List<PurchaseOrderLineForm> lines
) {
}