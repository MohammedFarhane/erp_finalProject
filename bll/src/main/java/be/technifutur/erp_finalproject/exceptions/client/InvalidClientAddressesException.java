package be.technifutur.erp_finalproject.exceptions.client;

import be.technifutur.erp_finalproject.exceptions.ConflictException;
import lombok.Getter;

@Getter
public class InvalidClientAddressesException extends ConflictException {

    private final Long clientId;

    private InvalidClientAddressesException(Long clientId, String message) {
        super(message);
        this.clientId = clientId;
    }

    public static InvalidClientAddressesException multipleBilling(Long clientId) {
        return new InvalidClientAddressesException(clientId,
                "Le client " + clientId + " ne peut avoir qu'une seule adresse de facturation");
    }

    public static InvalidClientAddressesException missingDelivery(Long clientId) {
        return new InvalidClientAddressesException(clientId,
                "Le client " + clientId + " doit avoir au moins une adresse de livraison");
    }
}