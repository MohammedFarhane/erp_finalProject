package be.technifutur.erp_finalproject.repositories;

import be.technifutur.erp_finalproject.entities.StockMovement;
import be.technifutur.erp_finalproject.projections.ProductStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    @Query("""                                                                                                                                                                   
              select coalesce(sum(
              case when m.type in (MovementType.SORTIE,
                                       MovementType.AJUSTEMENT_NEGATIF)
                       then -m.quantity
                       else m.quantity
                  end), 0)
              from StockMovement m
              where m.product.id = :productId
              """)
    int computeStockForProduct(@Param("productId") Long productId);

    @Query("""                                                                                                                                                                      
          select m.product.id as productId,
                 sum(case when m.type in (MovementType.SORTIE,
                                          MovementType.AJUSTEMENT_NEGATIF)
                          then -m.quantity
                          else m.quantity
                     end) as stock
          from StockMovement m
          where m.product.id in :productIds
          group by m.product.id
          """)
    List<ProductStock> computeStocksFor(@Param("productIds") List<Long> productIds);

    @EntityGraph(attributePaths = {"user"})
    Page<StockMovement> findByProductIdOrderByMovementDateDesc(Long productId, Pageable pageable);
}