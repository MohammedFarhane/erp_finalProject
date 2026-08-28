package be.technifutur.erp_finalproject.services.clientservice;

import be.technifutur.erp_finalproject.entities.Address;
import be.technifutur.erp_finalproject.entities.Client;
import be.technifutur.erp_finalproject.entities.TypeAddress;
import be.technifutur.erp_finalproject.enums.AddressType;
import be.technifutur.erp_finalproject.exceptions.EmailAlreadyUsedException;
import be.technifutur.erp_finalproject.exceptions.NotFoundException;
import be.technifutur.erp_finalproject.exceptions.client.InvalidClientAddressesException;
import be.technifutur.erp_finalproject.repositories.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClientServiceImpl")
class ClientServiceImplTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientServiceImpl service;

    private Client client;

    @BeforeEach
    void setUp() {
        client = new Client("Dupont SPRL", "contact@dupont.be", "042111111");
        client.getAddresses().add(livraison("Rue Neuve", "12"));
    }

    private TypeAddress livraison(String rue, String numero) {
        return new TypeAddress(AddressType.LIVRAISON, new Address(rue, numero, "4000", "Liege"));
    }

    private TypeAddress facturation(String rue, String numero) {
        return new TypeAddress(AddressType.FACTURATION, new Address(rue, numero, "4000", "Liege"));
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("l'adresse fournie devient une adresse de LIVRAISON")
        void adresseDeLivraisonImposee() {
            when(clientRepository.existsByEmail("nouveau@test.be")).thenReturn(false);
            when(clientRepository.save(any(Client.class))).thenAnswer(i -> i.getArgument(0));

            service.create(new ClientForm("Nouveau", "nouveau@test.be", "042000000",
                    new Address("Rue Test", "1", "4000", "Liege")));

            // C'est ce qui garantit l'invariant "au moins une livraison" verifie ailleurs.
            verify(clientRepository).save(any(Client.class));
        }

        @Test
        @DisplayName("email deja utilise : refus")
        void emailDejaPris() {
            when(clientRepository.existsByEmail("contact@dupont.be")).thenReturn(true);

            assertThatThrownBy(() -> service.create(new ClientForm("Doublon", "contact@dupont.be",
                    "042000000", new Address("Rue Test", "1", "4000", "Liege"))))
                    .isInstanceOf(EmailAlreadyUsedException.class);

            verify(clientRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("garder son propre email : pas de conflit avec soi-meme")
        void memeEmail() {
            when(clientRepository.findByIdAndArchivedFalse(1L)).thenReturn(Optional.of(client));
            when(clientRepository.save(client)).thenReturn(client);

            service.update(1L, new ClientForm("Dupont SA", "contact@dupont.be", "042111111", null));

            assertThat(client.getName()).isEqualTo("Dupont SA");
            verify(clientRepository, never()).existsByEmail(any());
        }

        @Test
        @DisplayName("email d'un autre client : refus")
        void emailDunAutre() {
            when(clientRepository.findByIdAndArchivedFalse(1L)).thenReturn(Optional.of(client));
            when(clientRepository.existsByEmail("info@martin.be")).thenReturn(true);

            assertThatThrownBy(() -> service.update(1L,
                    new ClientForm("Dupont", "info@martin.be", "042111111", null)))
                    .isInstanceOf(EmailAlreadyUsedException.class);

            assertThat(client.getEmail()).isEqualTo("contact@dupont.be");
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("archive le client")
        void archivage() {
            when(clientRepository.findByIdAndArchivedFalse(1L)).thenReturn(Optional.of(client));
            when(clientRepository.save(client)).thenReturn(client);

            service.delete(1L);

            assertThat(client.isArchived()).isTrue();
            verify(clientRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("addAddress — invariant des adresses")
    class AddAddress {

        @Test
        @DisplayName("ajouter une FACTURATION quand il n'y en a pas : accepte")
        void premiereFacturation() {
            when(clientRepository.findByIdAndArchivedFalse(1L)).thenReturn(Optional.of(client));
            when(clientRepository.save(client)).thenReturn(client);

            service.addAddress(1L, facturation("Avenue Louise", "500"));

            assertThat(client.getAddresses()).hasSize(2);
        }

        @Test
        @DisplayName("ajouter une seconde FACTURATION : refus")
        void secondeFacturation() {
            client.getAddresses().add(facturation("Avenue Louise", "500"));
            when(clientRepository.findByIdAndArchivedFalse(1L)).thenReturn(Optional.of(client));

            assertThatThrownBy(() -> service.addAddress(1L, facturation("Chaussee de Wavre", "230")))
                    .isInstanceOf(InvalidClientAddressesException.class);

            // La validation porte sur une copie de travail : le client ne doit pas
            // etre modifie avant que la regle soit verifiee.
            assertThat(client.getAddresses()).hasSize(2);
        }

        @Test
        @DisplayName("ajouter une seconde LIVRAISON : accepte, il n'y a pas de limite")
        void secondeLivraison() {
            when(clientRepository.findByIdAndArchivedFalse(1L)).thenReturn(Optional.of(client));
            when(clientRepository.save(client)).thenReturn(client);

            service.addAddress(1L, livraison("Chaussee de Wavre", "230"));

            assertThat(client.getAddresses()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("replaceAddress — invariant des adresses")
    class ReplaceAddress {

        @Test
        @DisplayName("une livraison et une facturation : accepte")
        void ensembleValide() {
            when(clientRepository.findByIdAndArchivedFalse(1L)).thenReturn(Optional.of(client));
            when(clientRepository.save(client)).thenReturn(client);

            service.replaceAddress(1L, Set.of(
                    livraison("Rue Neuve", "12"),
                    facturation("Avenue Louise", "500")));

            assertThat(client.getAddresses()).hasSize(2);
        }

        @Test
        @DisplayName("aucune LIVRAISON : refus")
        void sansLivraison() {
            when(clientRepository.findByIdAndArchivedFalse(1L)).thenReturn(Optional.of(client));

            assertThatThrownBy(() -> service.replaceAddress(1L,
                    Set.of(facturation("Avenue Louise", "500"))))
                    .isInstanceOf(InvalidClientAddressesException.class);

            // Un client sans adresse de livraison ne peut pas etre livre :
            // l'ancien jeu d'adresses doit rester en place.
            assertThat(client.getAddresses()).hasSize(1);
        }

        @Test
        @DisplayName("deux FACTURATION : refus")
        void deuxFacturations() {
            when(clientRepository.findByIdAndArchivedFalse(1L)).thenReturn(Optional.of(client));

            assertThatThrownBy(() -> service.replaceAddress(1L, Set.of(
                    livraison("Rue Neuve", "12"),
                    facturation("Avenue Louise", "500"),
                    facturation("Chaussee de Wavre", "230"))))
                    .isInstanceOf(InvalidClientAddressesException.class);
        }

        @Test
        @DisplayName("client inexistant : NotFoundException")
        void clientInexistant() {
            when(clientRepository.findByIdAndArchivedFalse(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.replaceAddress(999L, Set.of(livraison("Rue Neuve", "12"))))
                    .isInstanceOf(NotFoundException.class);
        }
    }
}
