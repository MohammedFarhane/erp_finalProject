package be.technifutur.erp_finalproject.services.clientservice;

import be.technifutur.erp_finalproject.entities.Address;

public record ClientForm(
        String name,
        String email,
        String phone,
        Address address
) {
}
