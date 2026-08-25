package be.technifutur.erp_finalproject.services.quoteservice;

import be.technifutur.erp_finalproject.entities.Quote;
import be.technifutur.erp_finalproject.entities.QuoteLine;

import java.util.List;

public record QuoteWithLines(
        Quote quote,
        List<QuoteLine> lines
) {
}
