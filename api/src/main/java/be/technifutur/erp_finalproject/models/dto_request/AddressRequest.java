package be.technifutur.erp_finalproject.models.dto_request;

import be.technifutur.erp_finalproject.entities.Address;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotBlank @Size(max = 100)
        String street,

        @NotBlank @Size(max = 20)
        String number,

        @NotBlank @Size(max = 100)
        String postalCode,

        @NotBlank @Size(max = 100)
        String locality
) {
    public Address toAddress() {
        return new Address(
                street,
                number,
                postalCode,
                locality
        );
    }
}
