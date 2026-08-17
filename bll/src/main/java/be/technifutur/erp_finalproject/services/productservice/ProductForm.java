package be.technifutur.erp_finalproject.services.productservice;

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