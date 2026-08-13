package be.technifutur.erp_finalproject.exceptions;

public abstract class ConflictException extends RuntimeException {

    protected ConflictException(String message) {
        super(message);
    }
}
