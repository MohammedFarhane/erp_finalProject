package be.technifutur.erp_finalproject.services.stockmovementservice;

import be.technifutur.erp_finalproject.entities.Product;
import be.technifutur.erp_finalproject.entities.StockMovement;
import be.technifutur.erp_finalproject.entities.User;
import be.technifutur.erp_finalproject.enums.MovementType;
import be.technifutur.erp_finalproject.exceptions.user.UserNotFoundException;
import be.technifutur.erp_finalproject.exceptions.product.ProductNotFoundException;
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
                .orElseThrow(() -> new ProductNotFoundException(form.productId()));

        User user = userRepository.findById(form.userId())
                .orElseThrow(() -> new UserNotFoundException(form.userId()));

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
}
