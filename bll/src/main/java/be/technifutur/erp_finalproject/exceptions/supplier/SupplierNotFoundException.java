package be.technifutur.erp_finalproject.exceptions.supplier;

import be.technifutur.erp_finalproject.exceptions.NotFoundException;

public class SupplierNotFoundException extends NotFoundException {

    private final Long id;

    public SupplierNotFoundException(Long id) {
        super("Le fournisseur " + id + " n'existe pas");
        this.id = id;
    }
}
