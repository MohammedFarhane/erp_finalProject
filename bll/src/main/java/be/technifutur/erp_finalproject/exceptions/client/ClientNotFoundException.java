package be.technifutur.erp_finalproject.exceptions.client;

import be.technifutur.erp_finalproject.exceptions.NotFoundException;
import lombok.Getter;

@Getter
public class ClientNotFoundException extends NotFoundException {

    private final Long id;

    public ClientNotFoundException(Long id) {
        super("Le client " + id + " n'existe pas");
        this.id = id;
    }
}
