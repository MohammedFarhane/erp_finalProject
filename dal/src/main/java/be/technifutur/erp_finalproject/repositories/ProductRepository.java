package be.technifutur.erp_finalproject.repositories;

import be.technifutur.erp_finalproject.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByCategoryId(Long id);

    @Query("""
        select p from Product p
        where p.archived = false
          and (:categoryId is null or p.category.id = :categoryId)
          and (:pattern is null or lower(p.name) like :pattern)
        """)
    @EntityGraph(attributePaths = {"category"})
    Page<Product> search(@Param("categoryId") Long categoryId,
                         @Param("pattern") String pattern,
                         Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    Optional<Product> findByIdAndArchivedFalse(Long id);

    boolean existsByReference(String reference);
}