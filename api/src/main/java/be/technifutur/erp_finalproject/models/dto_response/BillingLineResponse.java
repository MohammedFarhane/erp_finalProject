package be.technifutur.erp_finalproject.models.dto_response;

import be.technifutur.erp_finalproject.entities.BillingLine;

import java.math.BigDecimal;

public record BillingLineResponse(
        String name,
        int quantity,
        BigDecimal unitPrice,
        double tvaRate,
        BigDecimal tvaAmount,
        BigDecimal totalLinePrice
) {
    public static BillingLineResponse from(BillingLine line) {
        return new BillingLineResponse(
                line.getProduct().getName(),
                line.getQuantity(),
                line.getUnitPrice(),
                line.getTvaRate(),
                line.getTvaAmount(),
                line.getTotalLinePrice()
        );
    }
}
