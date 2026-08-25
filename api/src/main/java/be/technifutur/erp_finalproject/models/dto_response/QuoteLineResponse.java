package be.technifutur.erp_finalproject.models.dto_response;

import be.technifutur.erp_finalproject.entities.QuoteLine;

import java.math.BigDecimal;

public record QuoteLineResponse(
        Long id,
        int quantity,
        BigDecimal unitPrice,
        double tvaRate,
        BigDecimal tvaAmount,
        BigDecimal totalLinePrice,
        String productName
) {
    public static QuoteLineResponse fromQuoteLine(QuoteLine line) {
        return new QuoteLineResponse(
                line.getId(),
                line.getQuantity(),
                line.getUnitPrice(),
                line.getTvaRate(),
                line.getTvaAmount(),
                line.getTotalLinePrice(),
                line.getProduct().getName()
        );
    }
}