package be.technifutur.erp_finalproject.exceptions.category;

import lombok.Getter;

@Getter
public class CategoryAlreadyExistsException extends RuntimeException {

    private final String name;

    public CategoryAlreadyExistsException(String name) {
        super("La catégorie " + name + " porte déjà ce nom");
        this.name = name;
    }
}