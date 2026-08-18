package be.technifutur.erp_finalproject.models.dto_request;

import be.technifutur.erp_finalproject.entities.TypeAddress;
import be.technifutur.erp_finalproject.enums.AddressType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record TypeAddressRequest(
        @NotNull
        AddressType type,

        @NotNull @Valid
        AddressRequest address
) {
    public TypeAddress toTypeAddress() {
        return new TypeAddress(
                type,
                address.toAddress()
        );
    }
}
