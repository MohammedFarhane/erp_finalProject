package be.technifutur.erp_finalproject.exceptions.category;

import lombok.Getter;

@Getter
public class CategoryNotFoundException extends RuntimeException {

    private final Long id;

    public CategoryNotFoundException(Long id) {
        super("La catégorie " + id + " n'existe pas");
        this.id = id;
    }

}
