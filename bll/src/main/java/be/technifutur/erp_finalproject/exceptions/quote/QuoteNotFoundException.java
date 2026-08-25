package be.technifutur.erp_finalproject.exceptions.quote;

import be.technifutur.erp_finalproject.exceptions.NotFoundException;

public class QuoteNotFoundException extends NotFoundException {

    private final Long id;

    public QuoteNotFoundException(Long id) {
        super("Le devis " + id + " n'existe pas");
        this.id = id;
    }
}
