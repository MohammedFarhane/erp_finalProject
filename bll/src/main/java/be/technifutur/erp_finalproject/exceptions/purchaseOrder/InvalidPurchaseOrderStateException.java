package be.technifutur.erp_finalproject.exceptions.purchaseOrder;

import be.technifutur.erp_finalproject.enums.PurchaseOrderState;
import be.technifutur.erp_finalproject.exceptions.ConflictException;

public class InvalidPurchaseOrderStateException extends ConflictException {

    public InvalidPurchaseOrderStateException(Long orderId, PurchaseOrderState state) {
        super("La commande " + orderId + " est à l'état " + state + " : opération impossible");
    }
}