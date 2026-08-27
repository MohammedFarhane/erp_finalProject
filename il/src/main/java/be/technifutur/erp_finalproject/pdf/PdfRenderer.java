package be.technifutur.erp_finalproject.pdf;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PdfRenderer {

    private final TemplateEngine templateEngine;

    public byte[] render(String templateName, Map<String, Object> model) {

        // 1. Générer le HTML via le moteur de template
        Context context = new Context();
        context.setVariables(model);
        String html = templateEngine.process(templateName, context);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {

            PdfRendererBuilder builder = new PdfRendererBuilder();

            builder.withHtmlContent(html, null); // baseUri à null, ou une URI si tu as des ressources relatives (images, css)
            builder.toStream(os);
            builder.run();

            return os.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Échec de la génération du PDF", e);
        }
    }
}