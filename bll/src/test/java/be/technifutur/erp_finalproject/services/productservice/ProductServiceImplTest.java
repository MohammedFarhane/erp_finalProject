package be.technifutur.erp_finalproject.services.productservice;

import be.technifutur.erp_finalproject.ReferenceGenerator;
import be.technifutur.erp_finalproject.entities.Category;
import be.technifutur.erp_finalproject.entities.Product;
import be.technifutur.erp_finalproject.exceptions.NotFoundException;
import be.technifutur.erp_finalproject.repositories.CategoryRepository;
import be.technifutur.erp_finalproject.repositories.ProductRepository;
import be.technifutur.erp_finalproject.repositories.StockMovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductServiceImpl")
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private StockMovementRepository stockMovementRepository;
    @Mock
    private ReferenceGenerator referenceGenerator;

    @InjectMocks
    private ProductServiceImpl service;

    private Category category;
    private Product produit;

    @BeforeEach
    void setUp() {
        category = new Category("Electronics");
        produit = new Product("PRD-2026-00001", "Iphone 15", "Smartphone",
                new BigDecimal("299.99"), new BigDecimal("499.99"), 0.21, 10, category);
    }

    private ProductForm unFormulaire() {
        return new ProductForm("Iphone 15", "Smartphone",
                new BigDecimal("299.99"), new BigDecimal("499.99"), 0.21, 10, 1L);
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("genere une reference et rattache la categorie")
        void creationReussie() {
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
            when(referenceGenerator.next("PRD")).thenReturn("PRD-2026-00042");
            when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

            service.create(unFormulaire());

            ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
            verify(productRepository).save(captor.capture());

            assertThat(captor.getValue().getReference()).isEqualTo("PRD-2026-00042");
            assertThat(captor.getValue().getCategory()).isSameAs(category);
            assertThat(captor.getValue().isArchived()).isFalse();
        }

        @Test
        @DisplayName("categorie inexistante : NotFoundException, aucune reference consommee")
        void categorieInexistante() {
            when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.create(new ProductForm("Test", "description",
                    new BigDecimal("10.00"), new BigDecimal("20.00"), 0.21, 5, 999L)))
                    .isInstanceOf(NotFoundException.class);

            // La categorie est chargee avant la generation de reference :
            // un echec ne doit pas consommer un numero du compteur annuel.
            verify(referenceGenerator, never()).next(any());
            verify(productRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("renvoie le produit avec son stock calcule")
        void avecStock() {
            when(productRepository.findByIdAndArchivedFalse(1L)).thenReturn(Optional.of(produit));
            when(stockMovementRepository.computeStockForProduct(1L)).thenReturn(35);

            ProductWithStock resultat = service.findById(1L);

            // Le stock n'est pas un champ du produit : il est agrege depuis le journal.
            assertThat(resultat.product()).isSameAs(produit);
            assertThat(resultat.stock()).isEqualTo(35);
        }

        @Test
        @DisplayName("produit archive : introuvable")
        void produitArchive() {
            when(productRepository.findByIdAndArchivedFalse(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findById(1L))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("modifie les champs et change de categorie")
        void modificationReussie() {
            Category books = new Category("Books");
            when(productRepository.findByIdAndArchivedFalse(1L)).thenReturn(Optional.of(produit));
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(books));
            when(productRepository.save(produit)).thenReturn(produit);
            when(stockMovementRepository.computeStockForProduct(1L)).thenReturn(10);

            service.update(1L, new ProductForm("Iphone 16", "Nouveau modele",
                    new BigDecimal("349.99"), new BigDecimal("599.99"), 0.21, 15, 2L));

            assertThat(produit.getName()).isEqualTo("Iphone 16");
            assertThat(produit.getSellingPrice()).isEqualByComparingTo("599.99");
            assertThat(produit.getCategory()).isSameAs(books);
            // La reference ne change jamais : elle identifie le produit dans les documents deja emis.
            assertThat(produit.getReference()).isEqualTo("PRD-2026-00001");
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("archive le produit au lieu de le supprimer")
        void archivage() {
            when(productRepository.findByIdAndArchivedFalse(1L)).thenReturn(Optional.of(produit));
            when(productRepository.save(produit)).thenReturn(produit);

            service.delete(1L);

            // Suppression physique impossible : le produit est reference par des lignes
            // de devis, de facture et des mouvements de stock deja enregistres.
            assertThat(produit.isArchived()).isTrue();
            verify(productRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("produit deja archive : NotFoundException")
        void dejaArchive() {
            when(productRepository.findByIdAndArchivedFalse(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.delete(1L))
                    .isInstanceOf(NotFoundException.class);
        }
    }
}
