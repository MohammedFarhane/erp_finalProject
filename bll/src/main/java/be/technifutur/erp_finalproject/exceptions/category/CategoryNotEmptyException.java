package be.technifutur.erp_finalproject.exceptions.category;

import lombok.Getter;

@Getter
public class CategoryNotEmptyException extends RuntimeException {

    private final Long id;

    public CategoryNotEmptyException(Long id) {
        super("La catégorie " + id + " contient des produits");
        this.id = id;
    }
}