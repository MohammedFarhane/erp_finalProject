package be.technifutur.erp_finalproject.repositories;

import be.technifutur.erp_finalproject.entities.QuoteLine;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuoteLineRepository extends JpaRepository<QuoteLine, Long> {

    @EntityGraph(attributePaths = {"product"})
    List<QuoteLine> findByQuoteId(Long quoteId);
}
