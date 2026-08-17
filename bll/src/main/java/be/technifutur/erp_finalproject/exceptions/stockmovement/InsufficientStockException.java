package be.technifutur.erp_finalproject.exceptions.stockmovement;

import be.technifutur.erp_finalproject.exceptions.ConflictException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class InsufficientStockException extends ConflictException {

    private final Long productId;
    private final int available;
    private final int requested;

    public InsufficientStockException(Long productId, int available, int requested) {
        super("Stock insuffisant pour le produit " + productId
                + " : " + available + " disponible(s), " + requested + " demandé(s)");
        this.productId = productId;
        this.available = available;
        this.requested = requested;
    }
}