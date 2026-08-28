package be.technifutur.erp_finalproject.services.categoryservice;

import be.technifutur.erp_finalproject.entities.Category;
import be.technifutur.erp_finalproject.exceptions.NotFoundException;
import be.technifutur.erp_finalproject.exceptions.category.CategoryAlreadyExistsException;
import be.technifutur.erp_finalproject.exceptions.category.CategoryNotEmptyException;
import be.technifutur.erp_finalproject.repositories.CategoryRepository;
import be.technifutur.erp_finalproject.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryServiceImpl")
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CategoryServiceImpl service;

    private Category electronics;

    @BeforeEach
    void setUp() {
        electronics = new Category("Electronics");
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("nom libre : la categorie est enregistree")
        void nomLibre() {
            when(categoryRepository.existsByName("Electronics")).thenReturn(false);
            when(categoryRepository.save(electronics)).thenReturn(electronics);

            service.save(electronics);

            verify(categoryRepository).save(electronics);
        }

        @Test
        @DisplayName("nom deja pris : CategoryAlreadyExistsException, rien n'est enregistre")
        void nomDejaPris() {
            when(categoryRepository.existsByName("Electronics")).thenReturn(true);

            assertThatThrownBy(() -> service.save(electronics))
                    .isInstanceOf(CategoryAlreadyExistsException.class);

            verify(categoryRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("renommer avec un nom libre : accepte")
        void renommage() {
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(electronics));
            when(categoryRepository.existsByName("High-Tech")).thenReturn(false);
            when(categoryRepository.save(electronics)).thenReturn(electronics);

            service.update(1L, new Category("High-Tech"));

            assertThat(electronics.getName()).isEqualTo("High-Tech");
        }

        @Test
        @DisplayName("garder le meme nom : pas de conflit avec soi-meme")
        void memeNom() {
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(electronics));
            when(categoryRepository.save(electronics)).thenReturn(electronics);

            // Sans le test d'egalite prealable, existsByName renverrait true
            // et la categorie ne pourrait jamais etre mise a jour sans changer de nom.
            service.update(1L, new Category("Electronics"));

            verify(categoryRepository, never()).existsByName(any());
        }

        @Test
        @DisplayName("nom deja porte par une autre : refus")
        void nomPrisParUneAutre() {
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(electronics));
            when(categoryRepository.existsByName("Books")).thenReturn(true);

            assertThatThrownBy(() -> service.update(1L, new Category("Books")))
                    .isInstanceOf(CategoryAlreadyExistsException.class);

            assertThat(electronics.getName()).isEqualTo("Electronics");
        }

        @Test
        @DisplayName("categorie inexistante : NotFoundException")
        void inexistante() {
            when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.update(999L, new Category("Books")))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("categorie vide : suppression effective")
        void categorieVide() {
            when(categoryRepository.existsById(1L)).thenReturn(true);
            when(productRepository.existsByCategoryId(1L)).thenReturn(false);

            service.delete(1L);

            // Suppression physique, contrairement aux produits et clients qui sont archives :
            // une categorie n'est referencee par aucun document commercial.
            verify(categoryRepository).deleteById(1L);
        }

        @Test
        @DisplayName("categorie contenant des produits : CategoryNotEmptyException")
        void categorieNonVide() {
            when(categoryRepository.existsById(1L)).thenReturn(true);
            when(productRepository.existsByCategoryId(1L)).thenReturn(true);

            assertThatThrownBy(() -> service.delete(1L))
                    .isInstanceOf(CategoryNotEmptyException.class);

            // Sans cette garde, la contrainte de cle etrangere remonterait en erreur SQL brute.
            verify(categoryRepository, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("categorie inexistante : NotFoundException")
        void inexistante() {
            when(categoryRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> service.delete(999L))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("categorie inexistante : NotFoundException")
        void inexistante() {
            when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findById(999L))
                    .isInstanceOf(NotFoundException.class);
        }
    }
}
