package be.technifutur.erp_finalproject.repositories;

import be.technifutur.erp_finalproject.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByCategoryId(Long id);
}
