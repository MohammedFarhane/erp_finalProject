package be.technifutur.erp_finalproject.models.dto_request;

import be.technifutur.erp_finalproject.services.companyservice.CompanyForm;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CompanyRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 100) String tvaNumber,
        @NotBlank @Size(max = 100) String iban,
        @NotBlank @Email @Size(max = 100) String email,
        @NotBlank @Size(max = 100) String phone,
        @NotNull @Valid AddressRequest address
) {
    public CompanyForm toForm() {
        return new CompanyForm(
                name,
                tvaNumber,
                iban,
                email,
                phone,
                address.toAddress()
        );
    }
}
