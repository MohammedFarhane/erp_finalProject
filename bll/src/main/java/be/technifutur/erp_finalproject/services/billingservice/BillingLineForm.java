package be.technifutur.erp_finalproject.services.billingservice;

public record BillingLineForm(
        Long productId,
        int quantity
) {
}
