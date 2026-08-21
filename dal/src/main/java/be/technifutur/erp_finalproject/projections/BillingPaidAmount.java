package be.technifutur.erp_finalproject.projections;

import java.math.BigDecimal;

public interface BillingPaidAmount {

    Long getBillingId();
    BigDecimal getPaidAmount();
}
