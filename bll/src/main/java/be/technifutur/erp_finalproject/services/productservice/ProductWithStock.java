package be.technifutur.erp_finalproject.services.productservice;

import be.technifutur.erp_finalproject.entities.Product;

public record ProductWithStock(
        Product product,
        int stock
) {
}