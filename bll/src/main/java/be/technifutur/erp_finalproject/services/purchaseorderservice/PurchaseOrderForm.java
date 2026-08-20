package be.technifutur.erp_finalproject.services.purchaseorderservice;

import be.technifutur.erp_finalproject.services.purchaseorderlineservice.PurchaseOrderLineForm;

import java.util.List;

public record PurchaseOrderForm(
    Long supplierId,
    Long userId,
    List<PurchaseOrderLineForm> lines
) {
}