package be.technifutur.erp_finalproject.exceptions.product;

import be.technifutur.erp_finalproject.exceptions.NotFoundException;
import lombok.Getter;

@Getter
public class ProductNotFoundException extends NotFoundException {

    private final Long id;

    public ProductNotFoundException(Long id) {
        super("Le produit " + id + " n'existe pas");
        this.id = id;
    }

}
