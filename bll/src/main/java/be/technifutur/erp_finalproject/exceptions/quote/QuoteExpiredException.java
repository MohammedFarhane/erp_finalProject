package be.technifutur.erp_finalproject.exceptions.quote;

import be.technifutur.erp_finalproject.exceptions.ConflictException;

import java.time.LocalDate;

public class QuoteExpiredException extends ConflictException {

    private final Long quoteId;
    private final LocalDate expirationDate;

    public QuoteExpiredException(Long quoteId, LocalDate expirationDate) {
        super("Le devis " + quoteId + " a expiré le " + expirationDate);
        this.quoteId = quoteId;
        this.expirationDate = expirationDate;
    }
}
