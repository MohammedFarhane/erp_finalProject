package be.technifutur.erp_finalproject.models.dto_response;

import be.technifutur.erp_finalproject.entities.Product;
import be.technifutur.erp_finalproject.services.productservice.ProductWithStock;

import java.math.BigDecimal;

public record ProductResponse (
        Long id,
        String reference,
        String name,
        String description,
        BigDecimal purchasePrice,
        BigDecimal sellingPrice,
        Double tvaRate,
        int stock,
        Integer minStockQuantity,
        String categoryName
) {
    public static ProductResponse from(ProductWithStock pws) {
        Product product = pws.product();
        return new ProductResponse(
                product.getId(),
                product.getReference(),
                product.getName(),
                product.getDescription(),
                product.getPurchasePrice(),
                product.getSellingPrice(),
                product.getTvaRate(),
                pws.stock(),
                product.getMinStockQuantity(),
                product.getCategory().getName()
        );
    }
}