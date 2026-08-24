package be.technifutur.erp_finalproject.services.billingservice;

import be.technifutur.erp_finalproject.ReferenceGenerator;
import be.technifutur.erp_finalproject.entities.*;
import be.technifutur.erp_finalproject.enums.BillingState;
import be.technifutur.erp_finalproject.exceptions.billing.BillingNotFoundException;
import be.technifutur.erp_finalproject.exceptions.billing.InvalidBillingStateException;
import be.technifutur.erp_finalproject.exceptions.billing.PaymentExceedsBalanceException;
import be.technifutur.erp_finalproject.exceptions.client.ClientNotFoundException;
import be.technifutur.erp_finalproject.exceptions.product.ProductNotFoundException;
import be.technifutur.erp_finalproject.exceptions.user.UserNotFoundException;
import be.technifutur.erp_finalproject.projections.BillingPaidAmount;
import be.technifutur.erp_finalproject.repositories.*;
import be.technifutur.erp_finalproject.services.billinglineservice.BillingLineForm;
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

        String referencePattern = (reference == null || reference.isBlank())
                ? null
                : "%" + reference.toLowerCase() + "%";

        String namePattern = (clientName == null || clientName.isBlank())
                ? null
                : "%" + clientName.toLowerCase() + "%";

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
                .orElseThrow(() -> new BillingNotFoundException(id));

        List<BillingLine> lines = billingLineRepository.findByBillingId(id);

        BigDecimal amount = paymentRepository.computeAmountForBilling(id);

        return new BillingWithLines(billing, lines, amount);
    }

    @Override
    @Transactional
    public Long create(BillingForm form) {

        Client client = clientRepository.findByIdAndArchivedFalse(form.clientId())
                .orElseThrow(() -> new ClientNotFoundException(form.clientId()));

        User user = userRepository.findById(form.userId())
                .orElseThrow(() -> new UserNotFoundException(form.userId()));

        List<BillingLine> lines = new ArrayList<>();

        for (BillingLineForm lineForm : form.lines()) {

            Product product = productRepository.findByIdAndArchivedFalse(lineForm.productId())
                    .orElseThrow(() -> new ProductNotFoundException(lineForm.productId()));

            BigDecimal unirPrice = product.getSellingPrice();

            double tvaRate = product.getTvaRate();

            BigDecimal totalPrice = unirPrice
                    .multiply(BigDecimal.valueOf(lineForm.quantity()))
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal tvaAmount = totalPrice
                    .multiply(BigDecimal.valueOf(tvaRate))
                    .setScale(2, RoundingMode.HALF_UP);

            lines.add(new BillingLine(lineForm.quantity(), unirPrice, tvaRate, tvaAmount, totalPrice, product));
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
                .orElseThrow(() -> new BillingNotFoundException(id));

        if (billing.getState() != BillingState.BROUILLON) {
            throw new InvalidBillingStateException(id, billing.getState());
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        List<BillingLine> lines = billingLineRepository.findByBillingId(id);

        stockMovementService.recordSale(billing, lines, user);

        billing.setState(BillingState.VALIDEE);

        return new BillingWithLines(billing, lines, paymentRepository.computeAmountForBilling(id));
    }

    @Override
    @Transactional
    public BillingWithLines pay(Long id, PaymentForm form) {

        Billing billing = billingRepository.findById(id)
                .orElseThrow(() -> new BillingNotFoundException(id));

        if (billing.getState() != BillingState.VALIDEE) {
            throw new InvalidBillingStateException(id, billing.getState());
        }

        User user = userRepository.findById(form.userId())
                .orElseThrow(() -> new UserNotFoundException(form.userId()));

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
                .orElseThrow(() -> new BillingNotFoundException(id));

        if (billing.getState() == BillingState.PAYEE || billing.getState() == BillingState.ANNULEE) {
            throw new InvalidBillingStateException(id, billing.getState());
        }

        List<BillingLine> lines = billingLineRepository.findByBillingId(id);

        if (billing.getState() == BillingState.VALIDEE) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException(userId));

            stockMovementService.recordReturn(billing, lines, user);
        }

        billing.setState(BillingState.ANNULEE);

        return new BillingWithLines(billing, lines, paymentRepository.computeAmountForBilling(id));
    }
}