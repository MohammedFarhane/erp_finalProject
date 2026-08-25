package be.technifutur.erp_finalproject.services.quoteservice;

import be.technifutur.erp_finalproject.entities.Quote;
import be.technifutur.erp_finalproject.enums.QuoteState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QuoteService {

    Page<Quote> search(String reference, String clientName, QuoteState state, Pageable pageable);

    QuoteWithLines findById(Long id);

    Long create(QuoteForm form);

    QuoteWithLines send (Long id);

    QuoteWithLines accept(Long id, Long userId);

    QuoteWithLines refuse(Long id);
}