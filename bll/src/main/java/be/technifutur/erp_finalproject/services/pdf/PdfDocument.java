package be.technifutur.erp_finalproject.services.pdf;

public record PdfDocument(
        String fileName,
        byte[] content
) {
}
