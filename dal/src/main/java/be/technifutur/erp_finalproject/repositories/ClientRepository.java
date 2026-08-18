package be.technifutur.erp_finalproject.repositories;

import be.technifutur.erp_finalproject.entities.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    @Query("""
            select c from Client c
            where c.archived = false
                and (:namePattern is null or lower(c.name) like :namePattern )
                and (:emailPattern is null or lower(c.email) like :emailPattern )
            """)
    @EntityGraph(attributePaths = {"addresses"})
    Page<Client> search (@Param("namePattern") String namePattern,
                         @Param("emailPattern") String emailPattern,
                         Pageable pageable
                         );

    @EntityGraph(attributePaths = {"addresses"})
    Optional<Client> findByIdAndArchivedFalse(Long id);

    boolean existsByEmail(String email);
}