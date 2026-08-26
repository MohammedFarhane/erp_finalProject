package be.technifutur.erp_finalproject.exceptions.quote;

import be.technifutur.erp_finalproject.enums.QuoteState;
import be.technifutur.erp_finalproject.exceptions.ConflictException;

public class InvalideQuoteStateException extends ConflictException {

    private final Long quoteId;
    private final QuoteState state;

    public InvalideQuoteStateException(Long quoteId, QuoteState state) {
        super("Le devis " + quoteId + " est à l'état " + state + " : opération impossible");
        this.quoteId = quoteId;
        this.state = state;
    }
}
