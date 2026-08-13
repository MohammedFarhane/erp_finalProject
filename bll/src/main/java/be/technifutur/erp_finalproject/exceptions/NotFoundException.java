package be.technifutur.erp_finalproject.exceptions;

public abstract class NotFoundException extends RuntimeException {

    protected NotFoundException(String message) {
        super(message);
    }
}
