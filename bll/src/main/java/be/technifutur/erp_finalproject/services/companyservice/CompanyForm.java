package be.technifutur.erp_finalproject.services.companyservice;

import be.technifutur.erp_finalproject.entities.Address;

public record CompanyForm(
        String name,
        String tvaNumber,
        String iban,
        String email,
        String phone,
        Address address
) {
}
