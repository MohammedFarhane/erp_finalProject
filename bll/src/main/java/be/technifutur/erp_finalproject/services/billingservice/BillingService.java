package be.technifutur.erp_finalproject.services.billingservice;

import be.technifutur.erp_finalproject.entities.Billing;
import be.technifutur.erp_finalproject.enums.BillingState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface BillingService {

    Page<Billing> search(String reference, String clientName, BillingState state,
                         LocalDate from, LocalDate to, Pageable pageable);

    BillingWithLines findById(Long id);

    Long create(BillingForm form);

    BillingWithLines validate(Long id, Long userId);

    BillingWithLines pay(Long id, PaymentForm form);

    BillingWithLines cancel(Long id, Long userId);
}
