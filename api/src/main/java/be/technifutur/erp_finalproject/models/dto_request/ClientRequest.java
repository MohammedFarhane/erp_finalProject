package be.technifutur.erp_finalproject.models.dto_request;

import be.technifutur.erp_finalproject.services.clientservice.ClientForm;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record ClientRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email @Size(max = 100) String email,
        @NotBlank @Size(max = 100) String phone,
        @NotNull @Valid AddressRequest address
) {
    public ClientForm toForm() {
        return new ClientForm(
                name,
                email,
                phone,
                address.toAddress()
        );
    }
}