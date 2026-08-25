package be.technifutur.erp_finalproject.services.quoteservice;

import java.math.BigDecimal;
import java.util.List;

public record QuoteForm(
        Long clientId,
        Long userId,
        BigDecimal discount,
        List<QuoteLineForm> lines
) {
}
