package be.technifutur.erp_finalproject.services.userservice;

import be.technifutur.erp_finalproject.entities.User;
import be.technifutur.erp_finalproject.enums.UserRole;
import be.technifutur.erp_finalproject.exceptions.user.InvalidCredentialsException;
import be.technifutur.erp_finalproject.exceptions.user.MinimumAdminRequiredException;
import be.technifutur.erp_finalproject.exceptions.user.UserAlreadyExistException;
import be.technifutur.erp_finalproject.exceptions.user.UserNotFoundException;
import be.technifutur.erp_finalproject.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<User> search(String name, String email, UserRole role, Pageable pageable) {

        String namePattern = (name == null || name.isBlank())
                ? null
                : "%" + name.toLowerCase() + "%";

        String emailPattern = (email == null || email.isBlank())
                ? null
                : "%" + email.toLowerCase() + "%";

        return userRepository.search(namePattern, emailPattern, role, pageable);
    }

    @Override
    public User findById(Long id) {

        return userRepository.findByIdAndArchivedFalse(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    @Transactional
    public Long create(UserForm form) {
        if (userRepository.existsByEmail(form.email())){
            throw new UserAlreadyExistException(form.email());
        }

        User user = new User(
                form.name(),
                form.email(),
                passwordEncoder.encode(form.password()),
                form.role()
        );

        return userRepository.save(user).getId();
    }

    @Override
    @Transactional
    public void archive(Long id) {

        User user = userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException(id));

        checkNotLastAdmin(user);

        user.setArchived(true);

        userRepository.save(user);

    }

    @Override
    @Transactional
    public void changePassword(Long id, String oldPassword, String newPassword) {

        User user = userRepository.findByIdAndArchivedFalse(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void update(Long id, UserUpdateForm form) {

        User user = userRepository.findByIdAndArchivedFalse(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (form.role() != UserRole.ADMIN){
            checkNotLastAdmin(user);
        }

        user.setName(form.name());
        user.setEmail(form.email());
        user.setRole(form.role());


        userRepository.save(user);
    }

    private void checkNotLastAdmin(User user) {

        if (user.getRole() != UserRole.ADMIN) {
            return;
        }

        if (userRepository.countByRoleAndArchivedFalse(UserRole.ADMIN) <= 1) {
            throw new MinimumAdminRequiredException();
        }
    }
}
