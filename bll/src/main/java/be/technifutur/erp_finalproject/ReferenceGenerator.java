package be.technifutur.erp_finalproject;

import be.technifutur.erp_finalproject.repositories.ReferenceCounterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ReferenceGenerator {

    private final ReferenceCounterRepository referenceCounterRepository;
    private final Clock clock;

    public String next(String prefix) {
        int year = LocalDate.now(clock).getYear();
        int number = referenceCounterRepository.nextNumber(year, prefix);
        return String.format("%s-%d-%05d", prefix, year, number);
    }
}