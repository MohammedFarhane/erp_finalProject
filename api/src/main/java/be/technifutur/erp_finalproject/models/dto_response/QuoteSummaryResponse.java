package be.technifutur.erp_finalproject.models.dto_response;

import be.technifutur.erp_finalproject.entities.Quote;
import be.technifutur.erp_finalproject.enums.QuoteState;

import java.math.BigDecimal;
import java.time.LocalDate;

public record QuoteSummaryResponse(
        Long quoteId,
        String reference,
        LocalDate quoteDate,
        LocalDate expirationDate,
        QuoteState state,
        BigDecimal totalPrice,
        String clientName
) {
    public static QuoteSummaryResponse from(Quote quote) {
        return new QuoteSummaryResponse(
                quote.getId(),
                quote.getReference(),
                quote.getQuoteDate(),
                quote.getExpirationDate(),
                quote.getState(),
                quote.getTotalPrice(),
                quote.getClient().getName()
        );
    }
}
