package be.technifutur.erp_finalproject.services.pdf;

public interface DocumentService {

    PdfDocument generateBillingToPdf(Long id);

    PdfDocument generateQuoteToPdf(Long id);

    PdfDocument generatePurchaseOrderToPdf(Long id);
}
