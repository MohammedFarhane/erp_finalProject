package be.technifutur.erp_finalproject.services.productService;

import java.math.BigDecimal;

public record ProductForm(
        String name,
        String description,
        BigDecimal purchasePrice,
        BigDecimal sellingPrice,
        double tvaRate,
        int minStockQuantity,
        Long categoryId
) {
}