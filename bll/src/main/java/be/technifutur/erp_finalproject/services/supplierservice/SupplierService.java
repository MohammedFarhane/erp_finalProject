package be.technifutur.erp_finalproject.services.supplierservice;

import be.technifutur.erp_finalproject.entities.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SupplierService {

    Page<Supplier> search(String name, String email, Pageable pageable);

    Supplier findById(Long id);

    Long create(SupplierForm form);

    Supplier update(Long id, SupplierForm form);

    void delete(Long id);
}
