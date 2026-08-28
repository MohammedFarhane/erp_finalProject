package be.technifutur.erp_finalproject.services.userservice;

import be.technifutur.erp_finalproject.entities.User;
import be.technifutur.erp_finalproject.enums.UserRole;
import be.technifutur.erp_finalproject.exceptions.EmailAlreadyUsedException;
import be.technifutur.erp_finalproject.exceptions.NotFoundException;
import be.technifutur.erp_finalproject.exceptions.user.InvalidCredentialsException;
import be.technifutur.erp_finalproject.exceptions.user.MinimumAdminRequiredException;
import be.technifutur.erp_finalproject.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User admin;
    private User employee;

    @BeforeEach
    void setUp() {
        admin = new User("Admin", "admin@admin.be", "$2a$10$hashadmin", UserRole.ADMIN);
        employee = new User("Employee", "employee@employee.be", "$2a$10$hashemploye", UserRole.EMPLOYEE);
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("hache le mot de passe avant de l'enregistrer")
        void hacheLeMotDePasse() {
            when(userRepository.existsByEmail("nouveau@test.be")).thenReturn(false);
            when(passwordEncoder.encode("motdepasse123")).thenReturn("$2a$10$hashcalcule");
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

            userService.create(new UserForm("Nouveau", "nouveau@test.be", "motdepasse123", UserRole.EMPLOYEE));

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());

            // Sans encode(), le mot de passe partirait en clair en base
            // et matches() echouerait ensuite a chaque connexion.
            assertThat(captor.getValue().getPassword()).isEqualTo("$2a$10$hashcalcule");
            assertThat(captor.getValue().getPassword()).isNotEqualTo("motdepasse123");
        }

        @Test
        @DisplayName("email deja pris : EmailAlreadyUsedException, rien n'est enregistre")
        void emailDejaPris() {
            when(userRepository.existsByEmail("admin@admin.be")).thenReturn(true);

            assertThatThrownBy(() -> userService.create(
                    new UserForm("Doublon", "admin@admin.be", "motdepasse123", UserRole.EMPLOYEE)))
                    .isInstanceOf(EmailAlreadyUsedException.class);

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("archive — règle du dernier administrateur")
    class Archive {

        @Test
        @DisplayName("dernier admin actif : refus")
        void refuseDArchiverLeDernierAdmin() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
            when(userRepository.countByRoleAndArchivedFalse(UserRole.ADMIN)).thenReturn(1L);

            assertThatThrownBy(() -> userService.archive(1L))
                    .isInstanceOf(MinimumAdminRequiredException.class);

            assertThat(admin.isArchived()).isFalse();
        }

        @Test
        @DisplayName("deux admins actifs : archivage autorise")
        void autoriseSiUnAutreAdminExiste() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
            when(userRepository.countByRoleAndArchivedFalse(UserRole.ADMIN)).thenReturn(2L);

            userService.archive(1L);

            assertThat(admin.isArchived()).isTrue();
        }

        @Test
        @DisplayName("employé : aucun comptage, la règle ne le concerne pas")
        void employeToujoursArchivable() {
            when(userRepository.findById(2L)).thenReturn(Optional.of(employee));

            userService.archive(2L);

            assertThat(employee.isArchived()).isTrue();
            verify(userRepository, never()).countByRoleAndArchivedFalse(any());
        }

        @Test
        @DisplayName("utilisateur inexistant : NotFoundException")
        void utilisateurInexistant() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.archive(999L))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("update — règle du dernier administrateur")
    class Update {

        @Test
        @DisplayName("rétrograder le dernier admin en employé : refus")
        void refuseDeRetrograderLeDernierAdmin() {
            when(userRepository.findByIdAndArchivedFalse(1L)).thenReturn(Optional.of(admin));
            when(userRepository.countByRoleAndArchivedFalse(UserRole.ADMIN)).thenReturn(1L);

            assertThatThrownBy(() -> userService.update(1L,
                    new UserUpdateForm("Admin", "admin@admin.be", UserRole.EMPLOYEE)))
                    .isInstanceOf(MinimumAdminRequiredException.class);

            // La garde doit s'exécuter AVANT les setters : si elle passait apres,
            // elle testerait le nouveau role et laisserait passer ce cas.
            assertThat(admin.getRole()).isEqualTo(UserRole.ADMIN);
        }

        @Test
        @DisplayName("renommer le dernier admin sans changer son role : autorise")
        void renommerLeDernierAdmin() {
            when(userRepository.findByIdAndArchivedFalse(1L)).thenReturn(Optional.of(admin));

            userService.update(1L, new UserUpdateForm("Admin renomme", "admin@admin.be", UserRole.ADMIN));

            assertThat(admin.getName()).isEqualTo("Admin renomme");
            // Le role reste ADMIN : inutile de compter, sinon un simple renommage serait bloque.
            verify(userRepository, never()).countByRoleAndArchivedFalse(any());
        }

        @Test
        @DisplayName("rétrograder un admin quand un autre existe : autorise")
        void retrograderQuandUnAutreAdminExiste() {
            when(userRepository.findByIdAndArchivedFalse(1L)).thenReturn(Optional.of(admin));
            when(userRepository.countByRoleAndArchivedFalse(UserRole.ADMIN)).thenReturn(2L);

            userService.update(1L, new UserUpdateForm("Admin", "admin@admin.be", UserRole.EMPLOYEE));

            assertThat(admin.getRole()).isEqualTo(UserRole.EMPLOYEE);
        }

        @Test
        @DisplayName("promouvoir un employé en admin : autorise")
        void promouvoirUnEmploye() {
            when(userRepository.findByIdAndArchivedFalse(2L)).thenReturn(Optional.of(employee));

            userService.update(2L, new UserUpdateForm("Employee", "employee@employee.be", UserRole.ADMIN));

            assertThat(employee.getRole()).isEqualTo(UserRole.ADMIN);
        }
    }

    @Nested
    @DisplayName("changePassword")
    class ChangePassword {

        @Test
        @DisplayName("ancien mot de passe correct : le nouveau est hache")
        void changementReussi() {
            when(userRepository.findByIdAndArchivedFalse(1L)).thenReturn(Optional.of(admin));
            when(passwordEncoder.matches("test123", "$2a$10$hashadmin")).thenReturn(true);
            when(passwordEncoder.encode("nouveaumotdepasse")).thenReturn("$2a$10$nouveauhash");

            userService.changePassword(1L, "test123", "nouveaumotdepasse");

            assertThat(admin.getPassword()).isEqualTo("$2a$10$nouveauhash");
        }

        @Test
        @DisplayName("ancien mot de passe errone : refus, mot de passe inchange")
        void ancienMotDePasseErrone() {
            when(userRepository.findByIdAndArchivedFalse(1L)).thenReturn(Optional.of(admin));
            when(passwordEncoder.matches("mauvais", "$2a$10$hashadmin")).thenReturn(false);

            assertThatThrownBy(() -> userService.changePassword(1L, "mauvais", "nouveaumotdepasse"))
                    .isInstanceOf(InvalidCredentialsException.class);

            assertThat(admin.getPassword()).isEqualTo("$2a$10$hashadmin");
            verify(passwordEncoder, never()).encode(anyString());
        }

        @Test
        @DisplayName("compte archive : introuvable")
        void compteArchive() {
            when(userRepository.findByIdAndArchivedFalse(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.changePassword(1L, "test123", "nouveaumotdepasse"))
                    .isInstanceOf(NotFoundException.class);
        }
    }
}
