package be.technifutur.erp_finalproject.services.stockmovementservice;

import be.technifutur.erp_finalproject.entities.PurchaseOrder;
import be.technifutur.erp_finalproject.entities.PurchaseOrderLine;
import be.technifutur.erp_finalproject.entities.StockMovement;
import be.technifutur.erp_finalproject.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StockMovementService {

    Long record (StockMovementForm form);

    Page<StockMovement> history(Long productId, Pageable pageable);

    void recordReception(PurchaseOrder order, List<PurchaseOrderLine> lines, User user);
}
