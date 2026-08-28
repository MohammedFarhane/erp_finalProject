package be.technifutur.erp_finalproject.services.billingservice;

import be.technifutur.erp_finalproject.ReferenceGenerator;
import be.technifutur.erp_finalproject.entities.Billing;
import be.technifutur.erp_finalproject.entities.BillingLine;
import be.technifutur.erp_finalproject.entities.Category;
import be.technifutur.erp_finalproject.entities.Client;
import be.technifutur.erp_finalproject.entities.Payment;
import be.technifutur.erp_finalproject.entities.Product;
import be.technifutur.erp_finalproject.entities.Quote;
import be.technifutur.erp_finalproject.entities.QuoteLine;
import be.technifutur.erp_finalproject.entities.User;
import be.technifutur.erp_finalproject.enums.BillingState;
import be.technifutur.erp_finalproject.enums.PaymentMethod;
import be.technifutur.erp_finalproject.enums.UserRole;
import be.technifutur.erp_finalproject.exceptions.InvalidStateException;
import be.technifutur.erp_finalproject.exceptions.NotFoundException;
import be.technifutur.erp_finalproject.exceptions.billing.PaymentExceedsBalanceException;
import be.technifutur.erp_finalproject.repositories.BillingLineRepository;
import be.technifutur.erp_finalproject.repositories.BillingRepository;
import be.technifutur.erp_finalproject.repositories.PaymentRepository;
import be.technifutur.erp_finalproject.repositories.UserRepository;
import be.technifutur.erp_finalproject.services.stockmovementservice.StockMovementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BillingServiceImpl")
class BillingServiceImplTest {

    private static final LocalDate AUJOURD_HUI = LocalDate.of(2026, 3, 15);
    private static final Clock CLOCK = Clock.fixed(
            AUJOURD_HUI.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

    @Mock
    private BillingRepository billingRepository;
    @Mock
    private BillingLineRepository billingLineRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ReferenceGenerator referenceGenerator;
    @Mock
    private StockMovementService stockMovementService;

    private BillingServiceImpl service;

    private Billing billing;
    private User user;
    private Client client;

    @BeforeEach
    void setUp() {
        service = new BillingServiceImpl(
                billingRepository, billingLineRepository, paymentRepository, null,
                userRepository, null, referenceGenerator, CLOCK, stockMovementService);

        user = new User("Admin", "admin@admin.be", "hash", UserRole.ADMIN);
        client = new Client("Dupont", "contact@dupont.be", "042111111");
        billing = new Billing("FAC-2026-00001", AUJOURD_HUI, null,
                new BigDecimal("100.00"), new BigDecimal("21.00"), new BigDecimal("121.00"), user, client);
    }

    @Nested
    @DisplayName("validate")
    class Validate {

        @Test
        @DisplayName("depuis BROUILLON : passe a VALIDEE et sort le stock")
        void validationReussie() {
            List<BillingLine> lines = List.of();
            when(billingRepository.findById(1L)).thenReturn(Optional.of(billing));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(billingLineRepository.findByBillingId(1L)).thenReturn(lines);
            when(paymentRepository.computeAmountForBilling(1L)).thenReturn(BigDecimal.ZERO);

            service.validate(1L, 1L);

            assertThat(billing.getState()).isEqualTo(BillingState.VALIDEE);
            verify(stockMovementService).recordSale(billing, lines, user);
        }

        @Test
        @DisplayName("deja validee : refus, aucun mouvement de stock")
        void dejaValidee() {
            billing.setState(BillingState.VALIDEE);
            when(billingRepository.findById(1L)).thenReturn(Optional.of(billing));

            assertThatThrownBy(() -> service.validate(1L, 1L))
                    .isInstanceOf(InvalidStateException.class);

            // Sans cette garde, valider deux fois sortirait le stock deux fois.
            verifyNoInteractions(stockMovementService);
        }
    }

    @Nested
    @DisplayName("pay")
    class Pay {

        @BeforeEach
        void factureValidee() {
            billing.setState(BillingState.VALIDEE);
        }

        @Test
        @DisplayName("paiement partiel : la facture reste VALIDEE")
        void paiementPartiel() {
            when(billingRepository.findById(1L)).thenReturn(Optional.of(billing));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(paymentRepository.computeAmountForBilling(1L)).thenReturn(BigDecimal.ZERO);
            when(billingLineRepository.findByBillingId(1L)).thenReturn(List.of());

            service.pay(1L, new PaymentForm(new BigDecimal("50.00"), PaymentMethod.VIREMENT, 1L));

            // Il n'existe pas d'etat PARTIELLEMENT_PAYEE : l'information est dans la somme
            // des versements, pas dans un etat supplementaire a maintenir.
            assertThat(billing.getState()).isEqualTo(BillingState.VALIDEE);
            verify(paymentRepository).save(any(Payment.class));
        }

        @Test
        @DisplayName("solde exactement atteint : passe a PAYEE")
        void soldeAtteint() {
            when(billingRepository.findById(1L)).thenReturn(Optional.of(billing));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(paymentRepository.computeAmountForBilling(1L)).thenReturn(new BigDecimal("21.00"));
            when(billingLineRepository.findByBillingId(1L)).thenReturn(List.of());

            // 21 deja verses + 100 = 121, soit le total exact
            service.pay(1L, new PaymentForm(new BigDecimal("100.00"), PaymentMethod.CARTE, 1L));

            assertThat(billing.getState()).isEqualTo(BillingState.PAYEE);
        }

        @Test
        @DisplayName("versement qui depasse le solde : refus, rien n'est enregistre")
        void tropPercu() {
            when(billingRepository.findById(1L)).thenReturn(Optional.of(billing));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(paymentRepository.computeAmountForBilling(1L)).thenReturn(new BigDecimal("100.00"));

            // 100 deja verses + 50 = 150 > 121
            assertThatThrownBy(() -> service.pay(1L,
                    new PaymentForm(new BigDecimal("50.00"), PaymentMethod.ESPECE, 1L)))
                    .isInstanceOf(PaymentExceedsBalanceException.class);

            verify(paymentRepository, never()).save(any());
            assertThat(billing.getState()).isEqualTo(BillingState.VALIDEE);
        }

        @Test
        @DisplayName("facture en BROUILLON : on ne paie pas ce qui n'est pas valide")
        void factureEnBrouillon() {
            billing.setState(BillingState.BROUILLON);
            when(billingRepository.findById(1L)).thenReturn(Optional.of(billing));

            assertThatThrownBy(() -> service.pay(1L,
                    new PaymentForm(new BigDecimal("50.00"), PaymentMethod.VIREMENT, 1L)))
                    .isInstanceOf(InvalidStateException.class);
        }

        @Test
        @DisplayName("le versement porte la date de l'horloge et le mode choisi")
        void contenuDuVersement() {
            when(billingRepository.findById(1L)).thenReturn(Optional.of(billing));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(paymentRepository.computeAmountForBilling(1L)).thenReturn(BigDecimal.ZERO);
            when(billingLineRepository.findByBillingId(1L)).thenReturn(List.of());

            service.pay(1L, new PaymentForm(new BigDecimal("50.00"), PaymentMethod.CHEQUE, 1L));

            ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
            verify(paymentRepository).save(captor.capture());

            assertThat(captor.getValue().getAmount()).isEqualByComparingTo("50.00");
            assertThat(captor.getValue().getPaymentMethod()).isEqualTo(PaymentMethod.CHEQUE);
            assertThat(captor.getValue().getPaymentDate()).isEqualTo(AUJOURD_HUI);
        }
    }

    @Nested
    @DisplayName("cancel")
    class Cancel {

        @Test
        @DisplayName("depuis VALIDEE : le stock est rendu")
        void annulationApresValidation() {
            billing.setState(BillingState.VALIDEE);
            List<BillingLine> lines = List.of();
            when(billingRepository.findById(1L)).thenReturn(Optional.of(billing));
            when(billingLineRepository.findByBillingId(1L)).thenReturn(lines);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(paymentRepository.computeAmountForBilling(1L)).thenReturn(BigDecimal.ZERO);

            service.cancel(1L, 1L);

            assertThat(billing.getState()).isEqualTo(BillingState.ANNULEE);
            verify(stockMovementService).recordReturn(billing, lines, user);
        }

        @Test
        @DisplayName("depuis BROUILLON : aucun stock a rendre, il n'est jamais sorti")
        void annulationDunBrouillon() {
            when(billingRepository.findById(1L)).thenReturn(Optional.of(billing));
            when(billingLineRepository.findByBillingId(1L)).thenReturn(List.of());
            when(paymentRepository.computeAmountForBilling(1L)).thenReturn(BigDecimal.ZERO);

            service.cancel(1L, 1L);

            assertThat(billing.getState()).isEqualTo(BillingState.ANNULEE);
            verifyNoInteractions(stockMovementService);
        }

        @Test
        @DisplayName("facture PAYEE : refus")
        void facturePayee() {
            billing.setState(BillingState.PAYEE);
            when(billingRepository.findById(1L)).thenReturn(Optional.of(billing));

            assertThatThrownBy(() -> service.cancel(1L, 1L))
                    .isInstanceOf(InvalidStateException.class);
        }

        @Test
        @DisplayName("facture deja annulee : refus")
        void dejaAnnulee() {
            billing.setState(BillingState.ANNULEE);
            when(billingRepository.findById(1L)).thenReturn(Optional.of(billing));

            assertThatThrownBy(() -> service.cancel(1L, 1L))
                    .isInstanceOf(InvalidStateException.class);
        }
    }

    @Nested
    @DisplayName("createFromQuote")
    class CreateFromQuote {

        @Test
        @DisplayName("recopie les montants du devis sans les recalculer")
        void montantsFiges() {
            Quote quote = new Quote("DEV-2026-00001", AUJOURD_HUI,
                    new BigDecimal("1449.97"), new BigDecimal("10.00"),
                    new BigDecimal("274.05"), new BigDecimal("1579.02"),
                    AUJOURD_HUI.plusDays(30), client, user);

            when(referenceGenerator.next("FAC")).thenReturn("FAC-2026-00001");
            when(billingRepository.save(any(Billing.class))).thenAnswer(i -> i.getArgument(0));

            Billing resultat = service.createFromQuote(quote, List.of(), user);

            // Un prix negocie reste le prix negocie : si le tarif catalogue a change
            // entre l'envoi du devis et son acceptation, la facture ne doit pas bouger.
            assertThat(resultat.getSubTotal()).isEqualByComparingTo("1449.97");
            assertThat(resultat.getAmountTva()).isEqualByComparingTo("274.05");
            assertThat(resultat.getTotalPrice()).isEqualByComparingTo("1579.02");
            assertThat(resultat.getDiscount()).isEqualByComparingTo("10.00");
        }

        @Test
        @DisplayName("la facture nait en BROUILLON avec sa propre reference")
        void etatEtReference() {
            Quote quote = new Quote("DEV-2026-00001", AUJOURD_HUI,
                    new BigDecimal("100.00"), null, new BigDecimal("21.00"), new BigDecimal("121.00"),
                    AUJOURD_HUI.plusDays(30), client, user);

            when(referenceGenerator.next("FAC")).thenReturn("FAC-2026-00007");
            when(billingRepository.save(any(Billing.class))).thenAnswer(i -> i.getArgument(0));

            Billing resultat = service.createFromQuote(quote, List.of(), user);

            assertThat(resultat.getState()).isEqualTo(BillingState.BROUILLON);
            assertThat(resultat.getReference()).isEqualTo("FAC-2026-00007");
        }

        @Test
        @DisplayName("chaque ligne du devis devient une ligne de facture, rattachee a celle-ci")
        void recopieDesLignes() {
            Product produit = new Product("PRD-2026-00001", "Iphone 15", "description",
                    new BigDecimal("299.99"), new BigDecimal("499.99"), 0.21, 10, new Category("Electronics"));
            Quote quote = new Quote("DEV-2026-00001", AUJOURD_HUI,
                    new BigDecimal("999.98"), null, new BigDecimal("210.00"), new BigDecimal("1209.98"),
                    AUJOURD_HUI.plusDays(30), client, user);
            QuoteLine quoteLine = new QuoteLine(2, new BigDecimal("499.99"), 0.21,
                    new BigDecimal("210.00"), new BigDecimal("999.98"), produit);

            when(referenceGenerator.next("FAC")).thenReturn("FAC-2026-00001");
            when(billingRepository.save(any(Billing.class))).thenAnswer(i -> i.getArgument(0));

            Billing resultat = service.createFromQuote(quote, List.of(quoteLine), user);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<BillingLine>> captor = ArgumentCaptor.forClass(List.class);
            verify(billingLineRepository).saveAll(captor.capture());

            assertThat(captor.getValue()).singleElement().satisfies(l -> {
                assertThat(l.getQuantity()).isEqualTo(2);
                assertThat(l.getUnitPrice()).isEqualByComparingTo("499.99");
                assertThat(l.getProduct()).isSameAs(produit);
                assertThat(l.getBilling()).isSameAs(resultat);
            });
        }

        @Test
        @DisplayName("aucune sortie de stock : elle a lieu a la validation, pas a la creation")
        void pasDeMouvementDeStock() {
            Quote quote = new Quote("DEV-2026-00001", AUJOURD_HUI,
                    new BigDecimal("100.00"), null, new BigDecimal("21.00"), new BigDecimal("121.00"),
                    AUJOURD_HUI.plusDays(30), client, user);

            when(referenceGenerator.next("FAC")).thenReturn("FAC-2026-00001");
            when(billingRepository.save(any(Billing.class))).thenAnswer(i -> i.getArgument(0));

            service.createFromQuote(quote, List.of(), user);

            verifyNoInteractions(stockMovementService);
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("facture inexistante : NotFoundException")
        void inexistante() {
            when(billingRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findById(999L))
                    .isInstanceOf(NotFoundException.class);
        }
    }
}
