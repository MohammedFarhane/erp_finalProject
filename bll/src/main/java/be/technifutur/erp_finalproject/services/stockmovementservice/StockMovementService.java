package be.technifutur.erp_finalproject.services.stockmovementservice;

import be.technifutur.erp_finalproject.entities.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StockMovementService {

    Long record (StockMovementForm form);

    Page<StockMovement> history(Long productId, Pageable pageable);
}
