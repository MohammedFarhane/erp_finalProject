package be.technifutur.erp_finalproject.models.dto_request;

import be.technifutur.erp_finalproject.services.productservice.ProductForm;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank
        String name,

        String description,

        @Positive @NotNull
        BigDecimal purchasePrice,

        @Positive @NotNull
        BigDecimal sellingPrice,

        @Positive @NotNull
        Double tvaRate,

        @NotNull @PositiveOrZero
        Integer minStockQuantity,

        @NotNull
        Long categoryId
){
    public ProductForm toForm() {
        return new ProductForm(
                name,
                description,
                purchasePrice,
                sellingPrice,
                tvaRate,
                minStockQuantity,
                categoryId
        );
    }
}
