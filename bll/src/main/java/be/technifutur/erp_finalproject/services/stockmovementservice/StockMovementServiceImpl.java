package be.technifutur.erp_finalproject.services.stockmovementservice;

import be.technifutur.erp_finalproject.entities.*;
import be.technifutur.erp_finalproject.enums.MovementType;
import be.technifutur.erp_finalproject.exceptions.Entities;
import be.technifutur.erp_finalproject.exceptions.NotFoundException;
import be.technifutur.erp_finalproject.exceptions.stockmovement.InsufficientStockException;
import be.technifutur.erp_finalproject.repositories.ProductRepository;
import be.technifutur.erp_finalproject.repositories.StockMovementRepository;
import be.technifutur.erp_finalproject.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockMovementServiceImpl implements StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final Clock clock;

    @Override
    @Transactional
    public Long record(StockMovementForm form) {
        Product product = productRepository.findByIdAndArchivedFalse(form.productId())
                .orElseThrow(() -> new NotFoundException(Entities.PRODUCT, form.productId()));

        User user = userRepository.findById(form.userId())
                .orElseThrow(() -> new NotFoundException(Entities.USER, form.userId()));

        if (form.type() == MovementType.SORTIE) {
            int stock = stockMovementRepository.computeStockForProduct(form.productId());
            if (stock < form.quantity()) {
                throw new InsufficientStockException(form.productId(), stock, form.quantity());
            }
        }

        StockMovement stockMovement = new StockMovement(form.type(), form.quantity(),
                LocalDateTime.now(clock), product, user);
        return stockMovementRepository.save(stockMovement).getId();
    }

    @Override
    public Page<StockMovement> history(Long productId, Pageable pageable) {
        return stockMovementRepository.findByProductIdOrderByMovementDateDesc(productId, pageable);
    }

    @Override
    @Transactional
    public void recordReception(PurchaseOrder order, List<PurchaseOrderLine> lines, User user) {

        LocalDateTime now = LocalDateTime.now(clock);

        List<StockMovement> movements = lines
                .stream()
                .map(line -> {
                    StockMovement movement = new StockMovement(
                            MovementType.ENTREE, line.getQuantity(), now, line.getProduct(), user);
                    movement.setPurchaseOrder(order);
                    return movement;
                })
                .toList();

        stockMovementRepository.saveAll(movements);
    }

    @Override
    @Transactional
    public void recordSale(Billing billing, List<BillingLine> lines, User user) {

        Map<Long, Integer> quantitiesByProduct = lines.stream()
                .collect(Collectors.groupingBy(
                        line -> line.getProduct().getId(),
                        Collectors.summingInt(BillingLine::getQuantity)));

        for (Map.Entry<Long, Integer> entry : quantitiesByProduct.entrySet()) {
            int stock = stockMovementRepository.computeStockForProduct(entry.getKey());

            if (stock < entry.getValue()) {
                throw new InsufficientStockException(entry.getKey(), stock, entry.getValue());
            }
        }

        LocalDateTime now = LocalDateTime.now(clock);

        List<StockMovement> movements = lines
                    .stream()
                    .map(l -> {
                        StockMovement movement = new StockMovement(
                                MovementType.SORTIE, l.getQuantity(), now, l.getProduct(), user);

                        movement.setBilling(billing);

                        return movement;
                    })
                    .toList();

        stockMovementRepository.saveAll(movements);
    }

    @Override
    @Transactional
    public void recordReturn(Billing billing, List<BillingLine> lines, User user) {

        LocalDateTime now = LocalDateTime.now(clock);

        List<StockMovement> movements = lines.stream()
                .map(line -> {
                    StockMovement movement = new StockMovement(
                            MovementType.RETOUR_CLIENT, line.getQuantity(), now, line.getProduct(), user);
                    movement.setBilling(billing);
                    return movement;
                })
                .toList();

        stockMovementRepository.saveAll(movements);
    }
}