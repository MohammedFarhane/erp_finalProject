package be.technifutur.erp_finalproject.exceptions.user;

import be.technifutur.erp_finalproject.exceptions.NotFoundException;
import lombok.Getter;

@Getter
public class UserNotFoundException extends NotFoundException {

    private final Long id;

    public UserNotFoundException(Long id) {
        super("L'utilisateur " + id + " n'existe pas");
        this.id = id;
    }
}