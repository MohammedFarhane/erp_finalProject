package be.technifutur.erp_finalproject.repositories;

import be.technifutur.erp_finalproject.entities.Billing;
import be.technifutur.erp_finalproject.enums.BillingState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface BillingRepository extends JpaRepository <Billing, Long>{

    @Query("""
           select b from Billing b
           where (:referencePattern is null or lower(b.reference) like :referencePattern)
            and (:namePattern is null or lower(b.client.name) like :namePattern)
            and (:state is null or b.state = :state)
            and (:from is null or b.billingDate >= :from)
            and (:to is null or b.billingDate <= :to)
           """)
    @EntityGraph(attributePaths = {"client"})
    Page<Billing> search(@Param("referencePattern") String referencePattern,
                         @Param("namePattern") String namePattern,
                         @Param("state") BillingState state,
                         @Param("from") LocalDate from,
                         @Param("to") LocalDate to,
                         Pageable pageable
    );

    @EntityGraph(attributePaths = {"client"})
    Optional<Billing> findById(Long id);
}
