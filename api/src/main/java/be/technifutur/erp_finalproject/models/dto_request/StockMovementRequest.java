package be.technifutur.erp_finalproject.models.dto_request;

import be.technifutur.erp_finalproject.enums.MovementType;
import be.technifutur.erp_finalproject.services.stockmovementservice.StockMovementForm;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StockMovementRequest (

        @NotNull
        Long productId,

        @NotNull
        MovementType type,

        @NotNull
        @Positive
        int quantity
){
    public StockMovementForm toForm(Long userId) {
        return new StockMovementForm(

                productId,
                type,
                quantity,
                userId
        );
    }
}