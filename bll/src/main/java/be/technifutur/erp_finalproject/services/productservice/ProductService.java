package be.technifutur.erp_finalproject.services.productservice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    Page<ProductWithStock> search (Long categoryId, String name, Pageable pageable);

    ProductWithStock findById (Long id);

    Long create (ProductForm form);

    ProductWithStock update (Long id, ProductForm form);

    void delete (Long id);
}
