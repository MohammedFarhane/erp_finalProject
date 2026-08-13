package be.technifutur.erp_finalproject.exceptions.category;

import be.technifutur.erp_finalproject.exceptions.NotFoundException;
import lombok.Getter;

@Getter
public class CategoryNotFoundException extends NotFoundException {

    private final Long id;

    public CategoryNotFoundException(Long id) {
        super("La catégorie " + id + " n'existe pas");
        this.id = id;
    }

}
