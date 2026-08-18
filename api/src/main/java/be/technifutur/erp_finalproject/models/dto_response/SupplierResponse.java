package be.technifutur.erp_finalproject.models.dto_response;

import be.technifutur.erp_finalproject.entities.Supplier;

public record SupplierResponse (
        Long id,
        String name,
        String email,
        String phone,
        AddressResponse address
){
    public static SupplierResponse fromSupplier(Supplier supplier) {
        return new SupplierResponse(
                supplier.getId(),
                supplier.getName(),
                supplier.getEmail(),
                supplier.getPhone(),
                AddressResponse.fromAddress(supplier.getAddress())
        );
    }
}