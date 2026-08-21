package be.technifutur.erp_finalproject.exceptions.billing;

import be.technifutur.erp_finalproject.enums.BillingState;
import be.technifutur.erp_finalproject.exceptions.ConflictException;

public class InvalidBillingStateException extends ConflictException {

    private final Long billingId;
    private final BillingState state;

    public InvalidBillingStateException(Long billingId, BillingState state) {
        super("La facture " + billingId + " est à l'état " + state + " : opération impossible");
        this.billingId = billingId;
        this.state = state;
    }
}
