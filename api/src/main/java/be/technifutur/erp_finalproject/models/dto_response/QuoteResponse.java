package be.technifutur.erp_finalproject.models.dto_response;

import be.technifutur.erp_finalproject.entities.Quote;
import be.technifutur.erp_finalproject.enums.QuoteState;
import be.technifutur.erp_finalproject.services.quoteservice.QuoteWithLines;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record QuoteResponse(
        Long id,
        String reference,
        QuoteState state,
        LocalDate quoteDate,
        BigDecimal subTotal,
        BigDecimal discount,
        BigDecimal amountTva,
        BigDecimal totalPrice,
        LocalDate expirationDate,
        Long billingId,
        String clientName,
        Long userId,
        List<QuoteLineResponse> lines
) {
    public static QuoteResponse from(QuoteWithLines qwl){
        Quote quote = qwl.quote();

        return new QuoteResponse(
                quote.getId(),
                quote.getReference(),
                quote.getState(),
                quote.getQuoteDate(),
                quote.getSubTotal(),
                quote.getDiscount(),
                quote.getAmountTva(),
                quote.getTotalPrice(),
                quote.getExpirationDate(),
                quote.getBilling() == null ? null : quote.getBilling().getId(),
                quote.getClient().getName(),
                quote.getUser().getId(),
                qwl.lines().stream().map(QuoteLineResponse::from).toList()
        );
    }
}
