package be.technifutur.erp_finalproject.repositories;

import be.technifutur.erp_finalproject.entities.BillingLine;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillingLineRepository extends JpaRepository<BillingLine, Long> {

    @EntityGraph(attributePaths = {"product"})
    List<BillingLine> findByBillingId (Long billingId);
}
