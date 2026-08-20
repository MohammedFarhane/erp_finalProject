package be.technifutur.erp_finalproject.exceptions.purchaseOrder;

import be.technifutur.erp_finalproject.exceptions.NotFoundException;

public class PurchaseOrderNotFoundException extends NotFoundException {

    public PurchaseOrderNotFoundException(Long id) {
        super("La commande " + id + " n'existe pas");
    }
}
