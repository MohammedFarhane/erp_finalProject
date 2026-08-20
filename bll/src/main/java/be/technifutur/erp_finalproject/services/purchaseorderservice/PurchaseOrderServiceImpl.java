package be.technifutur.erp_finalproject.services.purchaseorderservice;

import be.technifutur.erp_finalproject.ReferenceGenerator;
import be.technifutur.erp_finalproject.entities.*;
import be.technifutur.erp_finalproject.enums.PurchaseOrderState;
import be.technifutur.erp_finalproject.exceptions.product.ProductNotFoundException;
import be.technifutur.erp_finalproject.exceptions.purchaseOrder.InvalidPurchaseOrderStateException;
import be.technifutur.erp_finalproject.exceptions.purchaseOrder.PurchaseOrderNotFoundException;
import be.technifutur.erp_finalproject.exceptions.supplier.SupplierNotFoundException;
import be.technifutur.erp_finalproject.exceptions.user.UserNotFoundException;
import be.technifutur.erp_finalproject.repositories.*;
import be.technifutur.erp_finalproject.services.stockmovementservice.StockMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderLineRepository purchaseOrderLineRepository;
    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final StockMovementService stockMovementService;
    private final ReferenceGenerator referenceGenerator;
    private final Clock clock;

    @Override
    public Page<PurchaseOrder> search(String reference, String supplierName, PurchaseOrderState state, Pageable pageable) {

        String referencePattern = (reference == null || reference.isBlank())
                ? null
                : "%" + reference.toLowerCase() + "%";

        String supplierPattern = (supplierName == null || supplierName.isBlank())
                ? null
                : "%" + supplierName.toLowerCase() + "%";

        return purchaseOrderRepository.search(referencePattern, supplierPattern, state, pageable);
    }

    @Override
    public PurchaseOrderWithLines findById(Long id) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new PurchaseOrderNotFoundException(id));

        List<PurchaseOrderLine> lines = purchaseOrderLineRepository.findByPurchaseOrderId(id);

        return new PurchaseOrderWithLines(purchaseOrder, lines);
    }

    @Override
    @Transactional
    public Long create(PurchaseOrderForm form) {

        //charge le supplier
        Supplier supplier = supplierRepository.findByIdAndArchivedFalse(form.supplierId())
                .orElseThrow(() -> new SupplierNotFoundException(form.supplierId()));

        //charge l'user
        User user = userRepository.findById(form.userId())
                .orElseThrow(() -> new UserNotFoundException(form.userId()));

        //charge les lignes de commande
        List<PurchaseOrderLine> lines = form.lines()
                .stream()
                .map(lineForm -> {
                    Product product = productRepository.findByIdAndArchivedFalse(lineForm.productId())
                            .orElseThrow(() -> new ProductNotFoundException(lineForm.productId()));

                    //le prix d'achat est recopié (garde le tarif du jour) même si le produit change de prix
                    return new PurchaseOrderLine(lineForm.quantity(), product.getPurchasePrice(), product);
                })
                .toList();

        //calcul du total
        BigDecimal totalPrice = lines
                .stream()
                .map(line -> line.getUnitPrice().multiply(BigDecimal.valueOf(line.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        //génère l'objet commande avec ref (avec id) et date
        //la commande doit être sauvée avant ses lignes : elles portent sa clé étrangère
        PurchaseOrder order = purchaseOrderRepository.save(new PurchaseOrder(
                referenceGenerator.next("CMD"),
                LocalDate.now(clock),
                totalPrice,
                supplier,
                user
        ));

        //assemble et enregistre les lignes de commande
        lines.forEach(line -> line.setPurchaseOrder(order));
        purchaseOrderLineRepository.saveAll(lines);

        return order.getId();
    }

    @Override
    @Transactional
    public PurchaseOrderWithLines receive(Long id, Long userId) {

        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new PurchaseOrderNotFoundException(id));

        if (purchaseOrder.getState() != PurchaseOrderState.EN_ATTENTE) {
            throw new InvalidPurchaseOrderStateException(purchaseOrder.getId(), purchaseOrder.getState());
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        List<PurchaseOrderLine> lines = purchaseOrderLineRepository.findByPurchaseOrderId(id);

        stockMovementService.recordReception(purchaseOrder, lines, user);

        purchaseOrder.setState(PurchaseOrderState.RECUE);

        return new PurchaseOrderWithLines(purchaseOrder, lines);
    }

    @Override
    @Transactional
    public PurchaseOrderWithLines cancel(Long id) {

        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new PurchaseOrderNotFoundException(id));

        if (purchaseOrder.getState() != PurchaseOrderState.EN_ATTENTE){
            throw  new InvalidPurchaseOrderStateException(purchaseOrder.getId(), purchaseOrder.getState());
        }

        purchaseOrder.setState(PurchaseOrderState.ANNULEE);

        List<PurchaseOrderLine> lines = purchaseOrderLineRepository.findByPurchaseOrderId(id);

        return new PurchaseOrderWithLines(purchaseOrder, lines);
    }
}