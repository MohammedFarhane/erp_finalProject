package be.technifutur.erp_finalproject.services.quoteservice;

import be.technifutur.erp_finalproject.ReferenceGenerator;
import be.technifutur.erp_finalproject.SearchPattern;
import be.technifutur.erp_finalproject.entities.*;
import be.technifutur.erp_finalproject.enums.QuoteState;
import be.technifutur.erp_finalproject.exceptions.Entities;
import be.technifutur.erp_finalproject.exceptions.InvalidStateException;
import be.technifutur.erp_finalproject.exceptions.NotFoundException;
import be.technifutur.erp_finalproject.exceptions.quote.QuoteExpiredException;
import be.technifutur.erp_finalproject.repositories.*;
import be.technifutur.erp_finalproject.services.billingservice.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuoteServiceImpl implements QuoteService{

    private static final int QUOTE_EXPIRATION_DAYS = 30;

    private final QuoteRepository quoteRepository;
    private final QuoteLineRepository quoteLineRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final BillingService billingService;
    private final ReferenceGenerator referenceGenerator;
    private final Clock clock;

    @Override
    public Page<Quote> search(String reference, String clientName, QuoteState state, Pageable pageable) {

        String referencePattern = SearchPattern.like(reference);
        String namePattern = SearchPattern.like(clientName);

        return quoteRepository.search(referencePattern, namePattern, state, pageable);
    }

    @Override
    public QuoteWithLines findById(Long id) {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(Entities.QUOTE, id));

        List<QuoteLine> lines = quoteLineRepository.findByQuoteId(id);

        return new QuoteWithLines(quote, lines);
    }

    @Override
    @Transactional
    public Long create(QuoteForm form) {

        Client client = clientRepository.findByIdAndArchivedFalse(form.clientId())
                .orElseThrow(() -> new NotFoundException(Entities.CLIENT, form.clientId()));

        User user = userRepository.findById(form.userId())
                .orElseThrow(() -> new NotFoundException(Entities.USER, form.userId()));

        List<QuoteLine> lines = new ArrayList<>();

        for (QuoteLineForm lineForm : form.lines()) {

            Product product = productRepository.findByIdAndArchivedFalse(lineForm.productId())
                    .orElseThrow(() -> new NotFoundException(Entities.PRODUCT, lineForm.productId()));

            BigDecimal unitPrice = product.getSellingPrice();

            double tvaRate = product.getTvaRate();

            BigDecimal totalPrice = unitPrice
                    .multiply(BigDecimal.valueOf(lineForm.quantity()))
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal tvaAmount = totalPrice
                    .multiply(BigDecimal.valueOf(tvaRate))
                    .setScale(2, RoundingMode.HALF_UP);

            lines.add(new QuoteLine(lineForm.quantity(), unitPrice, tvaRate, tvaAmount, totalPrice, product));
        }

        BigDecimal subTotal = lines
                .stream()
                .map(QuoteLine::getTotalLinePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal tvaBrute = lines
                .stream()
                .map(QuoteLine::getTvaAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal coeff = BigDecimal.ONE;
        if (form.discount() != null && form.discount().compareTo(BigDecimal.ZERO) > 0) {
            coeff = BigDecimal.ONE.subtract(
                    form.discount().divide(new BigDecimal(100), 4, RoundingMode.HALF_UP));
        }

        BigDecimal amountTva = tvaBrute
                .multiply(coeff)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalPrice = subTotal
                .multiply(coeff)
                .add(amountTva)
                .setScale(2, RoundingMode.HALF_UP);

        LocalDate quoteDate = LocalDate.now(clock);
        LocalDate expirationDate = quoteDate.plusDays(QUOTE_EXPIRATION_DAYS);

        Quote quote = quoteRepository.save(new Quote(
                referenceGenerator.next("DEV"),
                quoteDate,
                subTotal,
                form.discount(),
                amountTva,
                totalPrice,
                expirationDate,
                client,
                user
        ));

        lines.forEach(line -> line.setQuote(quote));
        quoteLineRepository.saveAll(lines);

        return quote.getId();
    }

    @Override
    @Transactional
    public QuoteWithLines send(Long id) {

        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(Entities.QUOTE, id));

        if (quote.getState() != QuoteState.BROUILLON) {
            throw new InvalidStateException(Entities.QUOTE, id, quote.getState());
        }

        quote.setState(QuoteState.ENVOYE);

        List<QuoteLine> lines = quoteLineRepository.findByQuoteId(id);

        return new QuoteWithLines(quote, lines);
    }

    @Override
    @Transactional
    public QuoteWithLines accept(Long id, Long userId) {

        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(Entities.QUOTE, id));

        if (quote.getState() != QuoteState.ENVOYE) {
            throw new InvalidStateException(Entities.QUOTE, id, quote.getState());
        }

        if (quote.getExpirationDate().isBefore(LocalDate.now(clock))) {
            throw new QuoteExpiredException(id, quote.getExpirationDate());
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(Entities.USER, id));

        List<QuoteLine> lines = quoteLineRepository.findByQuoteId(id);

        Billing billing = billingService.createFromQuote(quote, lines, user);

        quote.setBilling(billing);

        quote.setState(QuoteState.ACCEPTE);

        return new QuoteWithLines(quote, lines);
    }

    @Override
    @Transactional
    public QuoteWithLines refuse(Long id) {

        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(Entities.QUOTE, id));

        if (quote.getState() != QuoteState.ENVOYE) {
            throw new InvalidStateException(Entities.QUOTE, id, quote.getState());
        }

        quote.setState(QuoteState.REFUSE);

        List<QuoteLine> lines = quoteLineRepository.findByQuoteId(id);

        return new QuoteWithLines(quote, lines);
    }
}
