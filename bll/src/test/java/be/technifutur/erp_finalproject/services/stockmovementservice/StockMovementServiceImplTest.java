package be.technifutur.erp_finalproject.services.stockmovementservice;

import be.technifutur.erp_finalproject.entities.Billing;
import be.technifutur.erp_finalproject.entities.BillingLine;
import be.technifutur.erp_finalproject.entities.Category;
import be.technifutur.erp_finalproject.entities.Product;
import be.technifutur.erp_finalproject.entities.PurchaseOrder;
import be.technifutur.erp_finalproject.entities.PurchaseOrderLine;
import be.technifutur.erp_finalproject.entities.StockMovement;
import be.technifutur.erp_finalproject.entities.User;
import be.technifutur.erp_finalproject.enums.MovementType;
import be.technifutur.erp_finalproject.enums.UserRole;
import be.technifutur.erp_finalproject.exceptions.NotFoundException;
import be.technifutur.erp_finalproject.exceptions.stockmovement.InsufficientStockException;
import be.technifutur.erp_finalproject.repositories.ProductRepository;
import be.technifutur.erp_finalproject.repositories.StockMovementRepository;
import be.technifutur.erp_finalproject.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockMovementServiceImpl")
class StockMovementServiceImplTest {

    private static final Clock CLOCK = Clock.fixed(
            LocalDate.of(2026, 3, 15).atStartOfDay(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault());

    @Mock
    private StockMovementRepository stockMovementRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductRepository productRepository;

    private StockMovementServiceImpl service;

    private Product produit;
    private User user;

    @BeforeEach
    void setUp() {
        service = new StockMovementServiceImpl(
                stockMovementRepository, userRepository, productRepository, CLOCK);

        produit = produitAvecId(1L, "Iphone 15");
        user = new User("Admin", "admin@admin.be", "hash", UserRole.ADMIN);
    }

    /**
     * L'identifiant est génère par la base : il n'existe pas sur une entité construite en memoire.
     * Le service en a besoin (groupingBy sur getProduct().getId()), d'ou l'injection par reflexion.
     */
    private Product produitAvecId(Long id, String nom) {
        Product product = new Product("PRD-2026-00001", nom, "description",
                new BigDecimal("100.00"), new BigDecimal("150.00"), 0.21, 5, new Category("Electronics"));
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }

    private BillingLine ligne(Product product, int quantite) {
        return new BillingLine(quantite, new BigDecimal("150.00"), 0.21,
                new BigDecimal("31.50"), new BigDecimal("150.00"), product);
    }

    @Nested
    @DisplayName("record — mouvement manuel")
    class Record {

        @Test
        @DisplayName("SORTIE avec stock suffisant : le mouvement est enregistre")
        void sortieAvecStockSuffisant() {
            when(productRepository.findByIdAndArchivedFalse(1L)).thenReturn(Optional.of(produit));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(stockMovementRepository.computeStockForProduct(1L)).thenReturn(10);
            when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(i -> i.getArgument(0));

            service.record(new StockMovementForm(1L, MovementType.SORTIE, 3, 1L));

            verify(stockMovementRepository).save(any(StockMovement.class));
        }

        @Test
        @DisplayName("SORTIE de 10 avec 3 en stock : InsufficientStockException, rien n'est enregistre")
        void sortieAvecStockInsuffisant() {
            when(productRepository.findByIdAndArchivedFalse(1L)).thenReturn(Optional.of(produit));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(stockMovementRepository.computeStockForProduct(1L)).thenReturn(3);

            assertThatThrownBy(() -> service.record(new StockMovementForm(1L, MovementType.SORTIE, 10, 1L)))
                    .isInstanceOf(InsufficientStockException.class);

            verify(stockMovementRepository, never()).save(any());
        }

        @Test
        @DisplayName("SORTIE de exactement le stock disponible : autorise")
        void sortieDeToutLeStock() {
            when(productRepository.findByIdAndArchivedFalse(1L)).thenReturn(Optional.of(produit));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(stockMovementRepository.computeStockForProduct(1L)).thenReturn(5);
            when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(i -> i.getArgument(0));

            // La garde est `stock < quantite` : sortir tout le stock doit passer.
            service.record(new StockMovementForm(1L, MovementType.SORTIE, 5, 1L));

            verify(stockMovementRepository).save(any(StockMovement.class));
        }

        @Test
        @DisplayName("ENTREE : aucun controle de stock")
        void entreeSansControle() {
            when(productRepository.findByIdAndArchivedFalse(1L)).thenReturn(Optional.of(produit));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(i -> i.getArgument(0));

            service.record(new StockMovementForm(1L, MovementType.ENTREE, 50, 1L));

            verify(stockMovementRepository, never()).computeStockForProduct(any());
        }

        @Test
        @DisplayName("AJUSTEMENT_NÉGATIF : aucun contrôle non plus")
        void ajustementNegatifSansControle() {
            when(productRepository.findByIdAndArchivedFalse(1L)).thenReturn(Optional.of(produit));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(i -> i.getArgument(0));

            // Un ajustement négatif corrige un écart d'inventaire : il peut rendre le stock négatif,
            // et c'est justement l'information qu'on veut voir apparaitre.
            service.record(new StockMovementForm(1L, MovementType.AJUSTEMENT_NEGATIF, 999, 1L));

            verify(stockMovementRepository, never()).computeStockForProduct(any());
        }

        @Test
        @DisplayName("produit archive ou inexistant : NotFoundException")
        void produitInexistant() {
            when(productRepository.findByIdAndArchivedFalse(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.record(new StockMovementForm(999L, MovementType.ENTREE, 1, 1L)))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("horodatage pris sur l'horloge injectee")
        void horodatage() {
            when(productRepository.findByIdAndArchivedFalse(1L)).thenReturn(Optional.of(produit));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(i -> i.getArgument(0));

            service.record(new StockMovementForm(1L, MovementType.ENTREE, 5, 1L));

            ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);
            verify(stockMovementRepository).save(captor.capture());

            assertThat(captor.getValue().getMovementDate())
                    .isEqualTo(LocalDateTime.now(CLOCK));
        }
    }

    @Nested
    @DisplayName("recordSale — sortie de stock a la validation d'une facture")
    class RecordSale {

        @Test
        @DisplayName("stock suffisant : une SORTIE par ligne, rattachée a la facture")
        void venteEnregistree() {
            when(stockMovementRepository.computeStockForProduct(1L)).thenReturn(10);
            Billing billing = new Billing("FAC-2026-00001", LocalDate.now(CLOCK), null,
                    new BigDecimal("100.00"), new BigDecimal("21.00"), new BigDecimal("121.00"), user, null);

            service.recordSale(billing, List.of(ligne(produit, 3)), user);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<StockMovement>> captor = ArgumentCaptor.forClass(List.class);
            verify(stockMovementRepository).saveAll(captor.capture());

            assertThat(captor.getValue()).singleElement().satisfies(m -> {
                assertThat(m.getType()).isEqualTo(MovementType.SORTIE);
                assertThat(m.getQuantity()).isEqualTo(3);
                assertThat(m.getBilling()).isSameAs(billing);
            });
        }

        @Test
        @DisplayName("stock insuffisant : exception et AUCUN mouvement enregistre")
        void stockInsuffisant() {
            when(stockMovementRepository.computeStockForProduct(1L)).thenReturn(2);

            assertThatThrownBy(() -> service.recordSale(null, List.of(ligne(produit, 5)), user))
                    .isInstanceOf(InsufficientStockException.class);

            // La verification se fait en deux temps : tout contrôler, puis tout écrire.
            // Sans cela, une facture a trois lignes pourrait en decrementer deux avant d'échouer.
            verify(stockMovementRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("deux lignes du meme produit : les quantités sont cumulées avant le contrôle")
        void deuxLignesMemeProduit() {
            when(stockMovementRepository.computeStockForProduct(1L)).thenReturn(7);

            // 4 + 5 = 9 > 7 : aucune ligne prise isolement ne depasse le stock,
            // c'est leur somme qui le fait. Sans le groupingBy, ce cas passerait.
            assertThatThrownBy(() -> service.recordSale(
                    null, List.of(ligne(produit, 4), ligne(produit, 5)), user))
                    .isInstanceOf(InsufficientStockException.class);

            verify(stockMovementRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("deux produits differents : chacun est controle separement")
        void deuxProduitsDifferents() {
            Product autre = produitAvecId(2L, "Samsung Galaxy");
            when(stockMovementRepository.computeStockForProduct(1L)).thenReturn(10);
            when(stockMovementRepository.computeStockForProduct(2L)).thenReturn(10);

            service.recordSale(null, List.of(ligne(produit, 3), ligne(autre, 4)), user);

            verify(stockMovementRepository).computeStockForProduct(1L);
            verify(stockMovementRepository).computeStockForProduct(2L);
            verify(stockMovementRepository).saveAll(any());
        }
    }

    @Nested
    @DisplayName("recordReception et recordReturn")
    class AutresMouvements {

        @Test
        @DisplayName("reception : des ENTREE rattachees a la commande")
        void reception() {
            PurchaseOrderLine ligne = new PurchaseOrderLine(10, new BigDecimal("100.00"), produit);
            PurchaseOrder order = new PurchaseOrder("CMD-2026-00001", LocalDate.now(CLOCK),
                    new BigDecimal("1000.00"), null, user);

            service.recordReception(order, List.of(ligne), user);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<StockMovement>> captor = ArgumentCaptor.forClass(List.class);
            verify(stockMovementRepository).saveAll(captor.capture());

            assertThat(captor.getValue()).singleElement().satisfies(m -> {
                assertThat(m.getType()).isEqualTo(MovementType.ENTREE);
                assertThat(m.getQuantity()).isEqualTo(10);
                assertThat(m.getPurchaseOrder()).isSameAs(order);
            });
        }

        @Test
        @DisplayName("retour : des RETOUR_CLIENT, sans controle de stock")
        void retour() {
            service.recordReturn(null, List.of(ligne(produit, 3)), user);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<StockMovement>> captor = ArgumentCaptor.forClass(List.class);
            verify(stockMovementRepository).saveAll(captor.capture());

            assertThat(captor.getValue()).singleElement().satisfies(m ->
                    assertThat(m.getType()).isEqualTo(MovementType.RETOUR_CLIENT));

            // Un retour fait rentrer de la marchandise : rien a verifier.
            verify(stockMovementRepository, never()).computeStockForProduct(any());
        }
    }
}
