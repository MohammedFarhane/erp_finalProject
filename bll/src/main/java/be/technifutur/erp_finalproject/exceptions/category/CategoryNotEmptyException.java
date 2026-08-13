package be.technifutur.erp_finalproject.exceptions.category;

import be.technifutur.erp_finalproject.exceptions.ConflictException;
import lombok.Getter;

@Getter
public class CategoryNotEmptyException extends ConflictException {

    private final Long id;

    public CategoryNotEmptyException(Long id) {
        super("La catégorie " + id + " contient des produits");
        this.id = id;
    }
}