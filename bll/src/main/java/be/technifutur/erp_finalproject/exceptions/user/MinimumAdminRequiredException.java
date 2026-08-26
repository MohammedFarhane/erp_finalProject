package be.technifutur.erp_finalproject.exceptions.user;

import be.technifutur.erp_finalproject.exceptions.ConflictException;

public class MinimumAdminRequiredException extends ConflictException {

    public MinimumAdminRequiredException() {
        super("Il faut au moins un administrateur");
    }
}
