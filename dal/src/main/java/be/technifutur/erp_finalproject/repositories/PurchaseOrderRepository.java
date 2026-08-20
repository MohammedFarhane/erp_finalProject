package be.technifutur.erp_finalproject.repositories;

import be.technifutur.erp_finalproject.entities.PurchaseOrder;
import be.technifutur.erp_finalproject.enums.PurchaseOrderState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository <PurchaseOrder, Long>{

    @Query("""                                                                                                                                                                      
          select po from PurchaseOrder po
          where (:referencePattern is null or lower(po.reference) like :referencePattern)
            and (:supplierPattern is null or lower(po.supplier.name) like :supplierPattern)
            and (:state is null or po.state = :state)
          """)
    @EntityGraph(attributePaths = {"supplier"})
    Page<PurchaseOrder> search (@Param("referencePattern") String referencePattern,
                                @Param("supplierPattern") String supplierPattern,
                                @Param("state") PurchaseOrderState state,
                                Pageable pageable
    );

    @EntityGraph(attributePaths = {"supplier"})
    Optional<PurchaseOrder> findById(Long id);
}