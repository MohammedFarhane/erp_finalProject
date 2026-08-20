package be.technifutur.erp_finalproject.repositories;

import be.technifutur.erp_finalproject.entities.PurchaseOrderLine;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseOrderLineRepository extends JpaRepository<PurchaseOrderLine, Long> {

    @EntityGraph(attributePaths = {"product"})
    List<PurchaseOrderLine> findByPurchaseOrderId(Long purchaseOrderId);
}
