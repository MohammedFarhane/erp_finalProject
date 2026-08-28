package be.technifutur.erp_finalproject.services.quoteservice;

import be.technifutur.erp_finalproject.entities.Billing;
import be.technifutur.erp_finalproject.entities.Client;
import be.technifutur.erp_finalproject.entities.Quote;
import be.technifutur.erp_finalproject.entities.User;
import be.technifutur.erp_finalproject.enums.QuoteState;
import be.technifutur.erp_finalproject.enums.UserRole;
import be.technifutur.erp_finalproject.exceptions.InvalidStateException;
import be.technifutur.erp_finalproject.exceptions.NotFoundException;
import be.technifutur.erp_finalproject.exceptions.quote.QuoteExpiredException;
import be.technifutur.erp_finalproject.repositories.QuoteLineRepository;
import be.technifutur.erp_finalproject.repositories.QuoteRepository;
import be.technifutur.erp_finalproject.repositories.UserRepository;
import be.technifutur.erp_finalproject.services.billingservice.BillingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuoteServiceImpl — transitions d'etat")
class QuoteServiceImplTest {

    // Horloge fixe : sans elle, un test sur l'expiration deviendrait faux avec le temps.
    private static final LocalDate AUJOURD_HUI = LocalDate.of(2026, 3, 15);
    private static final Clock CLOCK = Clock.fixed(
            AUJOURD_HUI.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

    @Mock
    private QuoteRepository quoteRepository;
    @Mock
    private QuoteLineRepository quoteLineRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BillingService billingService;

    private QuoteServiceImpl quoteService;

    private Quote quote;
    private User user;

    @BeforeEach
    void setUp() {
        // Le service prend une horloge : on l'injecte a la main plutot que par @InjectMocks,
        // pour fournir CLOCK et non un mock.
        quoteService = new QuoteServiceImpl(
                quoteRepository, quoteLineRepository, null, userRepository, null,
                billingService, null, CLOCK);

        user = new User("Admin", "admin@admin.be", "hash", UserRole.ADMIN);
        quote = unDevis(AUJOURD_HUI.plusDays(30));
    }

    private Quote unDevis(LocalDate expiration) {
        return new Quote("DEV-2026-00001", AUJOURD_HUI,
                new BigDecimal("100.00"), null, new BigDecimal("21.00"), new BigDecimal("121.00"),
                expiration, new Client("Dupont", "contact@dupont.be", "042111111"), user);
    }

    @Nested
    @DisplayName("send")
    class Send {

        @Test
        @DisplayName("depuis BROUILLON : passe a ENVOYE")
        void depuisBrouillon() {
            when(quoteRepository.findById(1L)).thenReturn(Optional.of(quote));
            when(quoteLineRepository.findByQuoteId(1L)).thenReturn(List.of());

            quoteService.send(1L);

            assertThat(quote.getState()).isEqualTo(QuoteState.ENVOYE);
        }

        @ParameterizedTest(name = "depuis {0}")
        @EnumSource(value = QuoteState.class, names = {"ENVOYE", "ACCEPTE", "REFUSE"})
        @DisplayName("depuis tout autre état : refus")
        void depuisAutreEtat(QuoteState etat) {
            quote.setState(etat);
            when(quoteRepository.findById(1L)).thenReturn(Optional.of(quote));

            assertThatThrownBy(() -> quoteService.send(1L))
                    .isInstanceOf(InvalidStateException.class);

            assertThat(quote.getState()).isEqualTo(etat);
        }
    }

    @Nested
    @DisplayName("refuse")
    class Refuse {

        @Test
        @DisplayName("depuis ENVOYE : passe a REFUSE")
        void depuisEnvoye() {
            quote.setState(QuoteState.ENVOYE);
            when(quoteRepository.findById(1L)).thenReturn(Optional.of(quote));
            when(quoteLineRepository.findByQuoteId(1L)).thenReturn(List.of());

            quoteService.refuse(1L);

            assertThat(quote.getState()).isEqualTo(QuoteState.REFUSE);
        }

        @Test
        @DisplayName("un BROUILLON ne peut pas etre refuse : le client ne l'a jamais recu")
        void depuisBrouillon() {
            when(quoteRepository.findById(1L)).thenReturn(Optional.of(quote));

            assertThatThrownBy(() -> quoteService.refuse(1L))
                    .isInstanceOf(InvalidStateException.class);
        }
    }

    @Nested
    @DisplayName("accept")
    class Accept {

        @Test
        @DisplayName("depuis ENVOYE et non expire : cree la facture et la rattache au devis")
        void acceptationReussie() {
            quote.setState(QuoteState.ENVOYE);
            Billing billing = new Billing("FAC-2026-00001", AUJOURD_HUI, null,
                    new BigDecimal("100.00"), new BigDecimal("21.00"), new BigDecimal("121.00"),
                    user, quote.getClient());

            when(quoteRepository.findById(1L)).thenReturn(Optional.of(quote));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(quoteLineRepository.findByQuoteId(1L)).thenReturn(List.of());
            when(billingService.createFromQuote(any(), any(), any())).thenReturn(billing);

            quoteService.accept(1L, 1L);

            assertThat(quote.getState()).isEqualTo(QuoteState.ACCEPTE);
            assertThat(quote.getBilling()).isSameAs(billing);
        }

        @Test
        @DisplayName("expire hier : QuoteExpiredException, aucune facture creee")
        void devisExpire() {
            quote = unDevis(AUJOURD_HUI.minusDays(1));
            quote.setState(QuoteState.ENVOYE);
            when(quoteRepository.findById(1L)).thenReturn(Optional.of(quote));

            assertThatThrownBy(() -> quoteService.accept(1L, 1L))
                    .isInstanceOf(QuoteExpiredException.class);

            verifyNoInteractions(billingService);
            assertThat(quote.getState()).isEqualTo(QuoteState.ENVOYE);
        }

        @Test
        @DisplayName("expire aujourd'hui : encore valable")
        void expireAujourdHui() {
            quote = unDevis(AUJOURD_HUI);
            quote.setState(QuoteState.ENVOYE);
            Billing billing = new Billing("FAC-2026-00001", AUJOURD_HUI, null,
                    new BigDecimal("100.00"), new BigDecimal("21.00"), new BigDecimal("121.00"),
                    user, quote.getClient());

            when(quoteRepository.findById(1L)).thenReturn(Optional.of(quote));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(quoteLineRepository.findByQuoteId(1L)).thenReturn(List.of());
            when(billingService.createFromQuote(any(), any(), any())).thenReturn(billing);

            // isBefore(aujourd'hui) est faux le jour meme : un devis valable "jusqu'au 15"
            // l'est encore le 15.
            quoteService.accept(1L, 1L);

            assertThat(quote.getState()).isEqualTo(QuoteState.ACCEPTE);
        }

        @Test
        @DisplayName("depuis BROUILLON : refus, l'état est vérifie avant l'expiration")
        void depuisBrouillon() {
            assertThat(quote.getState()).isEqualTo(QuoteState.BROUILLON);
            when(quoteRepository.findById(1L)).thenReturn(Optional.of(quote));

            assertThatThrownBy(() -> quoteService.accept(1L, 1L))
                    .isInstanceOf(InvalidStateException.class);

            verifyNoInteractions(billingService);
            verify(userRepository, never()).findById(anyLong());
        }

        @Test
        @DisplayName("devis inexistant : NotFoundException")
        void devisInexistant() {
            when(quoteRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> quoteService.accept(999L, 1L))
                    .isInstanceOf(NotFoundException.class);
        }
    }
}
