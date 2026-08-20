package be.technifutur.erp_finalproject.services.purchaseorderservice;

import be.technifutur.erp_finalproject.entities.PurchaseOrder;
import be.technifutur.erp_finalproject.entities.PurchaseOrderLine;

import java.util.List;

public record PurchaseOrderWithLines(
        PurchaseOrder order,
        List<PurchaseOrderLine> lines
) {
}