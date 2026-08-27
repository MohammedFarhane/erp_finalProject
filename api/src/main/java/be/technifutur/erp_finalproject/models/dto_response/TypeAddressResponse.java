package be.technifutur.erp_finalproject.models.dto_response;

import be.technifutur.erp_finalproject.entities.TypeAddress;
import be.technifutur.erp_finalproject.enums.AddressType;

public record TypeAddressResponse(
        AddressType type,
        AddressResponse address
) {
    public static TypeAddressResponse from(TypeAddress typeAddress) {
        return new TypeAddressResponse(
                typeAddress.getType(),
                AddressResponse.from(typeAddress.getAddress())
        );
    }
}
