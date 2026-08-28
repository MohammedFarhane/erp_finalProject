package be.technifutur.erp_finalproject.services.authservice;

import be.technifutur.erp_finalproject.entities.User;
import be.technifutur.erp_finalproject.enums.UserRole;
import be.technifutur.erp_finalproject.exceptions.user.InvalidCredentialsException;
import be.technifutur.erp_finalproject.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl.login")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("Admin", "admin@admin.be", "$2a$10$hashfictif", UserRole.ADMIN);
    }

    @Test
    @DisplayName("identifiants valides : renvoie l'utilisateur")
    void connexionReussie() {
        when(userRepository.findByEmailAndArchivedFalse("admin@admin.be")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("test123", "$2a$10$hashfictif")).thenReturn(true);

        assertThat(authService.login("admin@admin.be", "test123")).isSameAs(user);
    }

    @Test
    @DisplayName("email inconnu : InvalidCredentialsException")
    void emailInconnu() {
        when(userRepository.findByEmailAndArchivedFalse(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login("personne@nulle-part.be", "test123"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("email inconnu : le mot de passe n'est meme pas compare")
    void emailInconnuNeCompareRien() {
        when(userRepository.findByEmailAndArchivedFalse(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login("personne@nulle-part.be", "test123"))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("mot de passe errone : InvalidCredentialsException")
    void mauvaisMotDePasse() {
        when(userRepository.findByEmailAndArchivedFalse("admin@admin.be")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("mauvais", "$2a$10$hashfictif")).thenReturn(false);

        assertThatThrownBy(() -> authService.login("admin@admin.be", "mauvais"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("email inconnu et mot de passe errone donnent le MEME message")
    void memeMessageDansLesDeuxCas() {
        when(userRepository.findByEmailAndArchivedFalse("inconnu@test.be")).thenReturn(Optional.empty());
        when(userRepository.findByEmailAndArchivedFalse("admin@admin.be")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        String messageEmailInconnu = messageDe("inconnu@test.be", "test123");
        String messageMotDePasseFaux = messageDe("admin@admin.be", "mauvais");

        // Si ces deux messages divergent, un attaquant peut énumérer les comptes existants.
        assertThat(messageEmailInconnu).isEqualTo(messageMotDePasseFaux);
    }

    @Test
    @DisplayName("la requete exclut les comptes archives")
    void utiliseLaRequeteQuiFiltreLesArchives() {
        when(userRepository.findByEmailAndArchivedFalse("admin@admin.be")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        authService.login("admin@admin.be", "test123");

        // Le filtrage des comptes désactivés est porte par la requete, pas par le service :
        // ce test verrouille le choix de la methode appelee.
        verify(userRepository).findByEmailAndArchivedFalse("admin@admin.be");
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    @DisplayName("l'ordre des arguments de matches : mot de passe en clair d'abord, hash ensuite")
    void ordreDesArgumentsDeMatches() {
        when(userRepository.findByEmailAndArchivedFalse("admin@admin.be")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("test123", "$2a$10$hashfictif")).thenReturn(true);

        authService.login("admin@admin.be", "test123");

        // Inverse, l'appel compile et renvoie toujours false : personne ne pourrait se connecter.
        verify(passwordEncoder).matches("test123", "$2a$10$hashfictif");
    }

    private String messageDe(String email, String password) {
        try {
            authService.login(email, password);
            throw new AssertionError("une exception était attendue");
        } catch (InvalidCredentialsException e) {
            return e.getMessage();
        }
    }
}
