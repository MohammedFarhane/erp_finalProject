package be.technifutur.erp_finalproject.services.userservice;

import be.technifutur.erp_finalproject.entities.User;
import be.technifutur.erp_finalproject.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    Page<User> search(String name, String email, UserRole role, Pageable pageable);

    User findById(Long id);

    Long create (UserForm form);

    void archive(Long id);

    void changePassword(Long id, String oldPassword, String newPassword);

    void update(Long id, UserUpdateForm form);
}
