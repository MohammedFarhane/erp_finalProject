package be.technifutur.erp_finalproject.exceptions.supplier;

import be.technifutur.erp_finalproject.exceptions.ConflictException;

public class SupplierAlreadyExistsException extends ConflictException {

    private final String email;

    public SupplierAlreadyExistsException(String email) {
        super("L'email " + email + " est déjà utilisé");
        this.email = email;
    }
}
