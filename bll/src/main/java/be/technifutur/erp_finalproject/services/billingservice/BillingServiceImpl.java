package be.technifutur.erp_finalproject.services.billingservice;

import be.technifutur.erp_finalproject.ReferenceGenerator;
import be.technifutur.erp_finalproject.SearchPattern;
import be.technifutur.erp_finalproject.entities.*;
import be.technifutur.erp_finalproject.enums.BillingState;
import be.technifutur.erp_finalproject.exceptions.Entities;
import be.technifutur.erp_finalproject.exceptions.InvalidStateException;
import be.technifutur.erp_finalproject.exceptions.NotFoundException;
import be.technifutur.erp_finalproject.exceptions.billing.PaymentExceedsBalanceException;
import be.technifutur.erp_finalproject.projections.BillingPaidAmount;
import be.technifutur.erp_finalproject.repositories.*;
import be.technifutur.erp_finalproject.services.stockmovementservice.StockMovementService;
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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BillingServiceImpl implements BillingService{

    private final BillingRepository billingRepository;
    private final BillingLineRepository billingLineRepository;
    private final PaymentRepository paymentRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ReferenceGenerator referenceGenerator;
    private final Clock clock;
    private final StockMovementService stockMovementService;


    @Override
    public Page<BillingSummary> search(String reference, String clientName, BillingState state,
                                LocalDate from, LocalDate to, Pageable pageable) {

        String referencePattern = SearchPattern.like(reference);
        String namePattern = SearchPattern.like(clientName);

        Page<Billing> page = billingRepository.search(referencePattern, namePattern, state, from, to, pageable);

        List<Long> ids = page
                .getContent()
                .stream()
                .map(Billing::getId)
                .toList();

        if (ids.isEmpty()) {
            return page.map(billing -> new BillingSummary(billing, BigDecimal.ZERO));
        }

        Map<Long, BigDecimal> paidBilling = paymentRepository.computePaymentsForBilling(ids)
                .stream()
                .collect(Collectors.toMap(BillingPaidAmount::getBillingId, BillingPaidAmount::getPaidAmount));

        return page.map(billing ->
                new BillingSummary(billing, paidBilling.getOrDefault(billing.getId(), BigDecimal.ZERO)));
    }

    @Override
    public BillingWithLines findById(Long id) {
        Billing billing = billingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(Entities.BILLING, id));

        List<BillingLine> lines = billingLineRepository.findByBillingId(id);

        BigDecimal amount = paymentRepository.computeAmountForBilling(id);

        return new BillingWithLines(billing, lines, amount);
    }

    @Override
    @Transactional
    public Long create(BillingForm form) {

        Client client = clientRepository.findByIdAndArchivedFalse(form.clientId())
                .orElseThrow(() -> new NotFoundException(Entities.CLIENT, form.clientId()));

        User user = userRepository.findById(form.userId())
                .orElseThrow(() -> new NotFoundException(Entities.USER, form.userId()));

        List<BillingLine> lines = new ArrayList<>();

        for (BillingLineForm lineForm : form.lines()) {

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

            lines.add(new BillingLine(lineForm.quantity(), unitPrice, tvaRate, tvaAmount, totalPrice, product));
        }

        BigDecimal subTotal = lines
                .stream()
                .map(BillingLine::getTotalLinePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal tvaBrute = lines
                .stream()
                .map(BillingLine::getTvaAmount)
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

        Billing billing = billingRepository.save(new Billing(
                referenceGenerator.next("FAC"),
                LocalDate.now(clock),
                form.discount(),
                subTotal,
                amountTva,
                totalPrice,
                user,
                client
        ));

        lines.forEach(line -> line.setBilling(billing));
        billingLineRepository.saveAll(lines);

        return billing.getId();
    }

    @Override
    @Transactional
    public BillingWithLines validate(Long id, Long userId) {

        Billing billing = billingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(Entities.BILLING, id));

        if (billing.getState() != BillingState.BROUILLON) {
            throw new InvalidStateException(Entities.BILLING, id, billing.getState());
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(Entities.USER, id));

        List<BillingLine> lines = billingLineRepository.findByBillingId(id);

        stockMovementService.recordSale(billing, lines, user);

        billing.setState(BillingState.VALIDEE);

        return new BillingWithLines(billing, lines, paymentRepository.computeAmountForBilling(id));
    }

    @Override
    @Transactional
    public BillingWithLines pay(Long id, PaymentForm form) {

        Billing billing = billingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(Entities.BILLING, id));

        if (billing.getState() != BillingState.VALIDEE) {
            throw new InvalidStateException(Entities.BILLING, id, billing.getState());
        }

        User user = userRepository.findById(form.userId())
                .orElseThrow(() -> new NotFoundException(Entities.USER, id));

        BigDecimal alreadyPaid = paymentRepository.computeAmountForBilling(id);
        BigDecimal afterPayment = alreadyPaid.add(form.amount());

        if (afterPayment.compareTo(billing.getTotalPrice()) > 0) {
            BigDecimal remaining = billing.getTotalPrice().subtract(alreadyPaid);
            throw new PaymentExceedsBalanceException(id, remaining, form.amount());
        }

        LocalDate now = LocalDate.now(clock);

        paymentRepository.save(new Payment(form.amount(), now, form.method(), billing, user));

        if (afterPayment.compareTo(billing.getTotalPrice()) == 0) {
            billing.setState(BillingState.PAYEE);
        }

        return new BillingWithLines(billing, billingLineRepository.findByBillingId(id), afterPayment);

    }

    @Override
    @Transactional
    public BillingWithLines cancel(Long id, Long userId) {

        Billing billing = billingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(Entities.BILLING, id));

        if (billing.getState() == BillingState.PAYEE || billing.getState() == BillingState.ANNULEE) {
            throw new InvalidStateException(Entities.BILLING, id, billing.getState());
        }

        List<BillingLine> lines = billingLineRepository.findByBillingId(id);

        if (billing.getState() == BillingState.VALIDEE) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException(Entities.USER, id));

            stockMovementService.recordReturn(billing, lines, user);
        }

        billing.setState(BillingState.ANNULEE);

        return new BillingWithLines(billing, lines, paymentRepository.computeAmountForBilling(id));
    }

    @Override
    @Transactional
    public Billing createFromQuote(Quote quote, List<QuoteLine> quoteLines, User user) {
        Billing billing = billingRepository.save(new Billing(
                referenceGenerator.next("FAC"),
                LocalDate.now(clock),
                quote.getDiscount(),
                quote.getSubTotal(),
                quote.getAmountTva(),
                quote.getTotalPrice(),
                user,
                quote.getClient()
        ));

        List<BillingLine> lines = quoteLines
                .stream()
                .map(ql -> new BillingLine(
                        ql.getQuantity(),
                        ql.getUnitPrice(),
                        ql.getTvaRate(),
                        ql.getTvaAmount(),
                        ql.getTotalLinePrice(),
                        ql.getProduct()))
                .toList();

        lines.forEach(line -> line.setBilling(billing));

        billingLineRepository.saveAll(lines);

        return billing;
    }
}