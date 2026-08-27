package be.technifutur.erp_finalproject.exceptions;

public class EmailAlreadyUsedException extends ConflictException {

    public EmailAlreadyUsedException(String email) {
        super("L'email " + email + " est déjà utilisé");
    }
}
