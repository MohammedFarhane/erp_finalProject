package be.technifutur.erp_finalproject.services.purchaseorderservice;

import be.technifutur.erp_finalproject.ReferenceGenerator;
import be.technifutur.erp_finalproject.entities.Address;
import be.technifutur.erp_finalproject.entities.Category;
import be.technifutur.erp_finalproject.entities.Product;
import be.technifutur.erp_finalproject.entities.PurchaseOrder;
import be.technifutur.erp_finalproject.entities.PurchaseOrderLine;
import be.technifutur.erp_finalproject.entities.Supplier;
import be.technifutur.erp_finalproject.entities.User;
import be.technifutur.erp_finalproject.enums.PurchaseOrderState;
import be.technifutur.erp_finalproject.enums.UserRole;
import be.technifutur.erp_finalproject.exceptions.InvalidStateException;
import be.technifutur.erp_finalproject.exceptions.NotFoundException;
import be.technifutur.erp_finalproject.repositories.ProductRepository;
import be.technifutur.erp_finalproject.repositories.PurchaseOrderLineRepository;
import be.technifutur.erp_finalproject.repositories.PurchaseOrderRepository;
import be.technifutur.erp_finalproject.repositories.SupplierRepository;
import be.technifutur.erp_finalproject.repositories.UserRepository;
import be.technifutur.erp_finalproject.services.stockmovementservice.StockMovementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PurchaseOrderServiceImpl")
class PurchaseOrderServiceImplTest {

    private static final LocalDate AUJOURD_HUI = LocalDate.of(2026, 3, 15);
    private static final Clock CLOCK = Clock.fixed(
            AUJOURD_HUI.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;
    @Mock
    private PurchaseOrderLineRepository purchaseOrderLineRepository;
    @Mock
    private SupplierRepository supplierRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private StockMovementService stockMovementService;
    @Mock
    private ReferenceGenerator referenceGenerator;

    private PurchaseOrderServiceImpl service;

    private PurchaseOrder order;
    private User user;
    private Supplier supplier;
    private Product produit;

    @BeforeEach
    void setUp() {
        service = new PurchaseOrderServiceImpl(
                purchaseOrderRepository, purchaseOrderLineRepository, supplierRepository,
                userRepository, productRepository, stockMovementService, referenceGenerator, CLOCK);

        user = new User("Admin", "admin@admin.be", "hash", UserRole.ADMIN);
        supplier = new Supplier("Fournitures Belges", "contact@fournitures.be", "042345678",
                new Address("Rue de l'Industrie", "45", "4000", "Liege"));
        produit = new Product("PRD-2026-00001", "Iphone 15", "description",
                new BigDecimal("299.99"), new BigDecimal("499.99"), 0.21, 10, new Category("Electronics"));
        order = new PurchaseOrder("CMD-2026-00001", AUJOURD_HUI,
                new BigDecimal("2999.90"), supplier, user);
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("le prix d'achat est fige au tarif du jour")
        void prixFige() {
            when(supplierRepository.findByIdAndArchivedFalse(1L)).thenReturn(Optional.of(supplier));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(productRepository.findByIdAndArchivedFalse(1L)).thenReturn(Optional.of(produit));
            when(referenceGenerator.next("CMD")).thenReturn("CMD-2026-00001");
            when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(i -> i.getArgument(0));

            service.create(new PurchaseOrderForm(1L, 1L, List.of(new PurchaseOrderLineForm(1L, 10))));

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<PurchaseOrderLine>> captor = ArgumentCaptor.forClass(List.class);
            verify(purchaseOrderLineRepository).saveAll(captor.capture());

            // Le prix vient du produit au moment de la commande : s'il change ensuite,
            // la commande deja passee garde le tarif negocie.
            assertThat(captor.getValue()).singleElement().satisfies(l ->
                    assertThat(l.getUnitPrice()).isEqualByComparingTo("299.99"));
        }

        @Test
        @DisplayName("le total est la somme des lignes")
        void totalCalcule() {
            when(supplierRepository.findByIdAndArchivedFalse(1L)).thenReturn(Optional.of(supplier));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(productRepository.findByIdAndArchivedFalse(1L)).thenReturn(Optional.of(produit));
            when(referenceGenerator.next("CMD")).thenReturn("CMD-2026-00001");
            when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(i -> i.getArgument(0));

            service.create(new PurchaseOrderForm(1L, 1L, List.of(new PurchaseOrderLineForm(1L, 10))));

            ArgumentCaptor<PurchaseOrder> captor = ArgumentCaptor.forClass(PurchaseOrder.class);
            verify(purchaseOrderRepository).save(captor.capture());

            // 299.99 x 10
            assertThat(captor.getValue().getTotalPrice()).isEqualByComparingTo("2999.90");
        }

        @Test
        @DisplayName("la commande nait EN_ATTENTE : le stock ne bouge pas encore")
        void etatInitial() {
            when(supplierRepository.findByIdAndArchivedFalse(1L)).thenReturn(Optional.of(supplier));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(productRepository.findByIdAndArchivedFalse(1L)).thenReturn(Optional.of(produit));
            when(referenceGenerator.next("CMD")).thenReturn("CMD-2026-00001");
            when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(i -> i.getArgument(0));

            service.create(new PurchaseOrderForm(1L, 1L, List.of(new PurchaseOrderLineForm(1L, 10))));

            ArgumentCaptor<PurchaseOrder> captor = ArgumentCaptor.forClass(PurchaseOrder.class);
            verify(purchaseOrderRepository).save(captor.capture());

            assertThat(captor.getValue().getState()).isEqualTo(PurchaseOrderState.EN_ATTENTE);
            // Commander n'est pas recevoir : le stock n'entre qu'a la reception.
            verifyNoInteractions(stockMovementService);
        }

        @Test
        @DisplayName("fournisseur archive ou inexistant : NotFoundException")
        void fournisseurInexistant() {
            when(supplierRepository.findByIdAndArchivedFalse(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.create(
                    new PurchaseOrderForm(999L, 1L, List.of(new PurchaseOrderLineForm(1L, 10)))))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("receive")
    class Receive {

        @Test
        @DisplayName("depuis EN_ATTENTE : passe a RECUE et fait entrer le stock")
        void receptionReussie() {
            List<PurchaseOrderLine> lines = List.of(new PurchaseOrderLine(10, new BigDecimal("299.99"), produit));
            when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(purchaseOrderLineRepository.findByPurchaseOrderId(1L)).thenReturn(lines);

            service.receive(1L, 1L);

            assertThat(order.getState()).isEqualTo(PurchaseOrderState.RECUE);
            verify(stockMovementService).recordReception(order, lines, user);
        }

        @ParameterizedTest(name = "depuis {0}")
        @EnumSource(value = PurchaseOrderState.class, names = {"RECUE", "ANNULEE"})
        @DisplayName("depuis tout autre etat : refus, aucun stock n'entre")
        void depuisAutreEtat(PurchaseOrderState etat) {
            order.setState(etat);
            when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> service.receive(1L, 1L))
                    .isInstanceOf(InvalidStateException.class);

            // Sans cette garde, receptionner deux fois ferait entrer la marchandise en double.
            verifyNoInteractions(stockMovementService);
            assertThat(order.getState()).isEqualTo(etat);
        }
    }

    @Nested
    @DisplayName("cancel")
    class Cancel {

        @Test
        @DisplayName("depuis EN_ATTENTE : passe a ANNULEE")
        void annulationReussie() {
            when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(purchaseOrderLineRepository.findByPurchaseOrderId(1L)).thenReturn(List.of());

            service.cancel(1L);

            assertThat(order.getState()).isEqualTo(PurchaseOrderState.ANNULEE);
        }

        @Test
        @DisplayName("commande deja recue : refus")
        void dejaRecue() {
            order.setState(PurchaseOrderState.RECUE);
            when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(order));

            // La marchandise est entree en stock : l'annulation se ferait par un mouvement
            // de sortie, pas par un changement d'etat.
            assertThatThrownBy(() -> service.cancel(1L))
                    .isInstanceOf(InvalidStateException.class);
        }

        @Test
        @DisplayName("commande inexistante : NotFoundException")
        void commandeInexistante() {
            when(purchaseOrderRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.cancel(999L))
                    .isInstanceOf(NotFoundException.class);
        }
    }
}
