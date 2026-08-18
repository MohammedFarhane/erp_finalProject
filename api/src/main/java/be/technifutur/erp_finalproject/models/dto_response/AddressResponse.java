package be.technifutur.erp_finalproject.models.dto_response;

import be.technifutur.erp_finalproject.entities.Address;

public record AddressResponse(
        String street,
        String number,
        String postalCode,
        String locality
) {
    public static AddressResponse fromAddress(Address address) {

        if (address == null) return null;

        return new AddressResponse(
                address.getStreet(),
                address.getNumber(),
                address.getPostalCode(),
                address.getLocality()
        );
    }
}