package be.technifutur.erp_finalproject.repositories;

import be.technifutur.erp_finalproject.entities.ReferenceCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReferenceCounterRepository extends JpaRepository<ReferenceCounter, Long> {

    @Query(value = """
        insert into reference_counter (year, prefix, last_number)
        values (:year, :prefix, 1)
        on conflict (year, prefix)
        do update set last_number = reference_counter.last_number + 1
        returning last_number
        """, nativeQuery = true)
    int nextNumber(@Param("year") int year, @Param("prefix") String prefix);
}
