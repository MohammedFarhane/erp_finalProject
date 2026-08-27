package be.technifutur.erp_finalproject.exceptions;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String entity, Long id) {

        super(entity + " " + id + " n'existe pas");
    }

    public NotFoundException(String entity) {
        super(entity + " n'existe pas");
    }
}
