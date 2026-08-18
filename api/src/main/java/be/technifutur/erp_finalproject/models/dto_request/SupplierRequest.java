package be.technifutur.erp_finalproject.models.dto_request;

import be.technifutur.erp_finalproject.services.supplierservice.SupplierForm;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SupplierRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email @Size(max = 100) String email,
        @NotBlank @Size(max = 100) String phone,
        @NotNull @Valid AddressRequest address
) {
    public SupplierForm toForm() {
        return new SupplierForm(
                name,
                email,
                phone,
                address.toAddress()
        );
    }
}
