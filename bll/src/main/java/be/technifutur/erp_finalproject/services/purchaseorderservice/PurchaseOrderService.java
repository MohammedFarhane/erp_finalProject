package be.technifutur.erp_finalproject.services.purchaseorderservice;

import be.technifutur.erp_finalproject.entities.PurchaseOrder;
import be.technifutur.erp_finalproject.enums.PurchaseOrderState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PurchaseOrderService {

    Page<PurchaseOrder> search(String reference, String supplierName, PurchaseOrderState state,
                               Pageable pageable);

    PurchaseOrderWithLines findById(Long id);

    Long create(PurchaseOrderForm form);

    PurchaseOrderWithLines receive(Long id, Long userId);

    PurchaseOrderWithLines cancel(Long id);
}