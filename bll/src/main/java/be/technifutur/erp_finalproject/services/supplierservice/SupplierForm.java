package be.technifutur.erp_finalproject.services.supplierservice;

import be.technifutur.erp_finalproject.entities.Address;

public record SupplierForm(
        String name,
        String email,
        String phone,
        Address address
) {
}
