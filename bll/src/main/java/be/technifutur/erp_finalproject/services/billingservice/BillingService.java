package be.technifutur.erp_finalproject.services.billingservice;

import be.technifutur.erp_finalproject.entities.Billing;
import be.technifutur.erp_finalproject.entities.Quote;
import be.technifutur.erp_finalproject.entities.QuoteLine;
import be.technifutur.erp_finalproject.entities.User;
import be.technifutur.erp_finalproject.enums.BillingState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface BillingService {

    Page<BillingSummary> search(String reference, String clientName, BillingState state,
                         LocalDate from, LocalDate to, Pageable pageable);

    BillingWithLines findById(Long id);

    Long create(BillingForm form);

    BillingWithLines validate(Long id, Long userId);

    BillingWithLines pay(Long id, PaymentForm form);

    BillingWithLines cancel(Long id, Long userId);

    Billing createFromQuote(Quote quote, List<QuoteLine> lines, User user);
}
