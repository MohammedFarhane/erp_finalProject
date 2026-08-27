package be.technifutur.erp_finalproject.models.dto_response;

import be.technifutur.erp_finalproject.entities.Client;

import java.util.Set;
import java.util.stream.Collectors;

public record ClientResponse(
        Long id,
        String name,
        String email,
        String phone,
        Set<TypeAddressResponse> addresses,
        AddressResponse billingAddress
) {
    public static ClientResponse from(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getName(),
                client.getEmail(),
                client.getPhone(),
                client.getAddresses()
                        .stream()
                        .map(TypeAddressResponse::from)
                        .collect(Collectors.toSet()),
                AddressResponse.from(client.getBillingAddress()));
    }
}
