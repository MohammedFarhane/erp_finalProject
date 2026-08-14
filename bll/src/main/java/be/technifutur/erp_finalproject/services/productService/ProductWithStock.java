package be.technifutur.erp_finalproject.services.productService;

import be.technifutur.erp_finalproject.entities.Product;

public record ProductWithStock(
        Product product,
        int stock
) {
}