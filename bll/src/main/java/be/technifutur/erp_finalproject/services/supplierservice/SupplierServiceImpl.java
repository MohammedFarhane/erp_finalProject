package be.technifutur.erp_finalproject.services.supplierservice;

import be.technifutur.erp_finalproject.entities.Supplier;
import be.technifutur.erp_finalproject.exceptions.supplier.SupplierAlreadyExistsException;
import be.technifutur.erp_finalproject.exceptions.supplier.SupplierNotFoundException;
import be.technifutur.erp_finalproject.repositories.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;

    @Override
    public Page<Supplier> search(String name, String email, Pageable pageable) {

        String namePattern = (name == null || name.isBlank())
                ? null
                : "%" + name.toLowerCase() + "%";
        String emailPattern = (email == null || email.isBlank())
                ? null
                : "%" + email.toLowerCase() + "%";

        return supplierRepository.search(namePattern, emailPattern, pageable);

    }

    @Override
    public Supplier findById(Long id) {

        return supplierRepository.findByIdAndArchivedFalse(id)
                .orElseThrow(() -> new SupplierNotFoundException(id));
    }

    @Override
    @Transactional
    public Long create(SupplierForm form) {

        if (supplierRepository.existsByEmail(form.email())){
            throw new SupplierAlreadyExistsException(form.email());
        }

        Supplier supplier = new Supplier(
                form.name(),
                form.email(),
                form.phone(),
                form.address()
        );

        return supplierRepository.save(supplier).getId();
    }

    @Override
    @Transactional
    public Supplier update(Long id, SupplierForm form) {

        Supplier supplier = supplierRepository.findByIdAndArchivedFalse(id)
                .orElseThrow(() -> new SupplierNotFoundException(id));

        if (!supplier.getEmail().equals(form.email()) && supplierRepository.existsByEmail(form.email())){
            throw new SupplierAlreadyExistsException(form.email());
        }

        supplier.setName(form.name());
        supplier.setEmail(form.email());
        supplier.setPhone(form.phone());
        supplier.setAddress(form.address());

        return supplierRepository.save(supplier);
    }

    @Override
    @Transactional
    public void delete(Long id) {

        Supplier supplier = supplierRepository.findByIdAndArchivedFalse(id)
                .orElseThrow(() -> new SupplierNotFoundException(id));

        supplier.setArchived(true);

        supplierRepository.save(supplier);
    }
}