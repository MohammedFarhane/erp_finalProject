package be.technifutur.erp_finalproject.exceptions;

public class InvalidStateException extends ConflictException{

    public InvalidStateException(String entity, Long id, Enum<?> state) {
        super(entity + " " + id + " est à l'état " + state + " : opération impossible");
    }
}
