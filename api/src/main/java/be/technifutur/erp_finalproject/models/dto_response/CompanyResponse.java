package be.technifutur.erp_finalproject.models.dto_response;

import be.technifutur.erp_finalproject.entities.Company;

public record CompanyResponse(
        Long id,
        String name,
        String tvaNumber,
        String iban,
        String email,
        String phone,
        AddressResponse address
) {
    public static CompanyResponse from(Company company) {
        return new CompanyResponse(
                company.getId(),
                company.getName(),
                company.getTvaNumber(),
                company.getIban(),
                company.getEmail(),
                company.getPhone(),
                AddressResponse.from(company.getAddress())
        );
    }
}
