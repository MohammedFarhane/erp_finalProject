package be.technifutur.erp_finalproject.exceptions.category;

import be.technifutur.erp_finalproject.exceptions.ConflictException;
import lombok.Getter;

@Getter
public class CategoryAlreadyExistsException extends ConflictException {

    private final String name;

    public CategoryAlreadyExistsException(String name) {
        super("Une catégorie porte déjà le nom " + name);
        this.name = name;
    }
}