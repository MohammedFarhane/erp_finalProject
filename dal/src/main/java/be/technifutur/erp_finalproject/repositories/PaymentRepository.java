package be.technifutur.erp_finalproject.repositories;

import be.technifutur.erp_finalproject.entities.Payment;
import be.technifutur.erp_finalproject.projections.BillingPaidAmount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("""                                                                                                                                                                   
              select coalesce(sum(p.amount), 0)
              from Payment p
              where p.billing.id = :billingId
           """)
    BigDecimal computeAmountForBilling(@Param("billingId") Long billingId);

    @Query("""
              select p.billing.id as billingId,
                sum(p.amount) as paidAmount
              from Payment p
              where p.billing.id in :billingIds
              group by p.billing.id
           """)
    List<BillingPaidAmount> computePaymentsForBilling(@Param("billingIds") List<Long> billingIds);
}