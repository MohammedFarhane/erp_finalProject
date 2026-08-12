package be.technifutur.erp_finalproject.repositories;

import be.technifutur.erp_finalproject.entities.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
