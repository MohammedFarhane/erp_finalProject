package be.technifutur.erp_finalproject.services.supplierservice;

import be.technifutur.erp_finalproject.entities.Address;
import be.technifutur.erp_finalproject.entities.Supplier;
import be.technifutur.erp_finalproject.exceptions.EmailAlreadyUsedException;
import be.technifutur.erp_finalproject.exceptions.NotFoundException;
import be.technifutur.erp_finalproject.repositories.SupplierRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SupplierServiceImpl")
class SupplierServiceImplTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private SupplierServiceImpl service;

    private Supplier supplier;

    @BeforeEach
    void setUp() {
        supplier = new Supplier("Fournitures Belges", "contact@fournitures.be", "042345678",
                new Address("Rue de l'Industrie", "45", "4000", "Liege"));
    }

    private SupplierForm unFormulaire(String email) {
        return new SupplierForm("Fournitures Belges", email, "042345678",
                new Address("Rue de l'Industrie", "45", "4000", "Liege"));
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("email libre : le fournisseur est enregistre, actif")
        void creationReussie() {
            when(supplierRepository.existsByEmail("nouveau@test.be")).thenReturn(false);
            when(supplierRepository.save(any(Supplier.class))).thenAnswer(i -> i.getArgument(0));

            service.create(unFormulaire("nouveau@test.be"));

            verify(supplierRepository).save(any(Supplier.class));
        }

        @Test
        @DisplayName("email deja utilise : EmailAlreadyUsedException")
        void emailDejaPris() {
            when(supplierRepository.existsByEmail("contact@fournitures.be")).thenReturn(true);

            assertThatThrownBy(() -> service.create(unFormulaire("contact@fournitures.be")))
                    .isInstanceOf(EmailAlreadyUsedException.class);

            verify(supplierRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("garder son propre email : pas de conflit avec soi-meme")
        void memeEmail() {
            when(supplierRepository.findByIdAndArchivedFalse(1L)).thenReturn(Optional.of(supplier));
            when(supplierRepository.save(supplier)).thenReturn(supplier);

            service.update(1L, new SupplierForm("Fournitures Belges SA", "contact@fournitures.be",
                    "042345678", new Address("Rue de l'Industrie", "45", "4000", "Liege")));

            assertThat(supplier.getName()).isEqualTo("Fournitures Belges SA");
        }

        @Test
        @DisplayName("fournisseur archive : introuvable")
        void fournisseurArchive() {
            when(supplierRepository.findByIdAndArchivedFalse(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.update(1L, unFormulaire("autre@test.be")))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("archive le fournisseur au lieu de le supprimer")
        void archivage() {
            when(supplierRepository.findByIdAndArchivedFalse(1L)).thenReturn(Optional.of(supplier));
            when(supplierRepository.save(supplier)).thenReturn(supplier);

            service.delete(1L);

            // Les commandes fournisseur deja passees le referencent :
            // une suppression physique casserait leur historique.
            assertThat(supplier.isArchived()).isTrue();
            verify(supplierRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("deja archive : NotFoundException")
        void dejaArchive() {
            when(supplierRepository.findByIdAndArchivedFalse(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.delete(1L))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("fournisseur inexistant : NotFoundException")
        void inexistant() {
            when(supplierRepository.findByIdAndArchivedFalse(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findById(999L))
                    .isInstanceOf(NotFoundException.class);
        }
    }
}
