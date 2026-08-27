package be.technifutur.erp_finalproject.services.pdf;

import be.technifutur.erp_finalproject.pdf.PdfRenderer;
import be.technifutur.erp_finalproject.services.billingservice.BillingService;
import be.technifutur.erp_finalproject.services.billingservice.BillingWithLines;
import be.technifutur.erp_finalproject.services.companyservice.CompanyService;
import be.technifutur.erp_finalproject.services.purchaseorderservice.PurchaseOrderService;
import be.technifutur.erp_finalproject.services.purchaseorderservice.PurchaseOrderWithLines;
import be.technifutur.erp_finalproject.services.quoteservice.QuoteService;
import be.technifutur.erp_finalproject.services.quoteservice.QuoteWithLines;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentServiceImpl implements DocumentService {

    private final BillingService billingService;
    private final CompanyService companyService;
    private final QuoteService quoteService;
    private final PurchaseOrderService purchaseOrderService;
    private final PdfRenderer pdfRenderer;

    @Override
    public PdfDocument generateBillingToPdf(Long id) {

        BillingWithLines bwl = billingService.findById(id);

        Map<String, Object> model = Map.of(
                "company", companyService.find(),
                "billing", bwl.billing(),
                "lines", bwl.lines(),
                "paidAmount", bwl.paidAmount()
        );

        return new PdfDocument(
                bwl.billing().getReference() + ".pdf",
                pdfRenderer.render("billing", model)
        );
    }

    @Override
    public PdfDocument generateQuoteToPdf(Long id) {

        QuoteWithLines qwt = quoteService.findById(id);

        Map<String, Object> model = Map.of(
                "company", companyService.find(),
                "quote", qwt.quote(),
                "lines", qwt.lines()
        );

        return new PdfDocument(
                qwt.quote().getReference() + ".pdf",
                pdfRenderer.render("quote", model)
        );
    }

    @Override
    public PdfDocument generatePurchaseOrderToPdf(Long id) {

        PurchaseOrderWithLines powl = purchaseOrderService.findById(id);

        Map<String, Object> model = Map.of(
                "company", companyService.find(),
                "order", powl.order(),
                "lines", powl.lines()
        );

        return new PdfDocument(
                powl.order().getReference() + ".pdf",
                pdfRenderer.render("purchase-order", model)
        );
    }
}
