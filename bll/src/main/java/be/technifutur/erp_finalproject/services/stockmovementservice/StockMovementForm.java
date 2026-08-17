package be.technifutur.erp_finalproject.services.stockmovementservice;

import be.technifutur.erp_finalproject.enums.MovementType;
import lombok.Getter;

public record StockMovementForm (
        Long productId,
        MovementType type,
        int quantity,
        Long userId
){
}