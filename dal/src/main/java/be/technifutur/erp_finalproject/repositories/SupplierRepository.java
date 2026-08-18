package be.technifutur.erp_finalproject.repositories;

import be.technifutur.erp_finalproject.entities.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SupplierRepository extends JpaRepository <Supplier, Long>{

    @Query("""
            select s from Supplier s
            where s.archived = false
                and (:namePattern is null or lower(s.name) like :namePattern )
                and (:emailPattern is null or lower(s.email) like :emailPattern )
            """)
    Page<Supplier> search (
            @Param("namePattern") String name,
            @Param("emailPattern") String email,
            Pageable pageable
    );

    Optional<Supplier> findByIdAndArchivedFalse(Long id);

    boolean existsByEmail(String email);
}