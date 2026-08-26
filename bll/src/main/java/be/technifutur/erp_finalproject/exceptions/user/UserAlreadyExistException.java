package be.technifutur.erp_finalproject.exceptions.user;

import be.technifutur.erp_finalproject.exceptions.ConflictException;

public class UserAlreadyExistException extends ConflictException {

    private final String email;

    public UserAlreadyExistException(String email) {
            super("L'email " + email + " est déjà utilisé");
            this.email = email;    }
}
