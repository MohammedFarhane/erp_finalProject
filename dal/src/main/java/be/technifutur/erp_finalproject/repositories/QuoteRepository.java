package be.technifutur.erp_finalproject.repositories;

import be.technifutur.erp_finalproject.entities.Quote;
import be.technifutur.erp_finalproject.enums.QuoteState;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QuoteRepository extends JpaRepository<Quote, Long> {

    @Query("""
           select q from Quote q
            where (:referencePattern is null or lower(q.reference) like :referencePattern)
            and (:namePattern is null or lower(q.client.name) like :namePattern)
            and (:state is null or q.state = :state)
           """)
    @EntityGraph(attributePaths = {"client"})
    Page<Quote> search(@Param("referencePattern") String reference,
                       @Param("namePattern") String name,
                       @Param("state") QuoteState state,
                       Pageable pageable
    );

    @Override
    @EntityGraph(attributePaths = {"client"})
    Optional<Quote> findById(Long id);
}
