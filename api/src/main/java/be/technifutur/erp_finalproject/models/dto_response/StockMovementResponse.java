package be.technifutur.erp_finalproject.models.dto_response;

import be.technifutur.erp_finalproject.entities.StockMovement;
import be.technifutur.erp_finalproject.enums.MovementType;

import java.time.LocalDateTime;

public record StockMovementResponse(
        Long id,
        MovementType type,
        int quantity,
        LocalDateTime date,
        String userName
) {
    public static StockMovementResponse form(StockMovement stockMovement) {
        return new StockMovementResponse(
                stockMovement.getId(),
                stockMovement.getType(),
                stockMovement.getQuantity(),
                stockMovement.getMovementDate(),
                stockMovement.getUser().getName()
        );
    }
}
