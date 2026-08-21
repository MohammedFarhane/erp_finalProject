package be.technifutur.erp_finalproject.exceptions.billing;

import be.technifutur.erp_finalproject.exceptions.NotFoundException;

public class BillingNotFoundException extends NotFoundException {

    private final Long id;

    public BillingNotFoundException(Long id) {
        super("La facture " + id + " n'existe pas");
        this.id = id;
    }
}
