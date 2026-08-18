package be.technifutur.erp_finalproject.exceptions.client;

import be.technifutur.erp_finalproject.exceptions.ConflictException;
import lombok.Getter;

@Getter
public class ClientAlreadyExistsException extends ConflictException {

    private final String email;

    public ClientAlreadyExistsException(String email) {
        super("L'email " + email + " est déjà utilisé");
        this.email = email;
    }
}