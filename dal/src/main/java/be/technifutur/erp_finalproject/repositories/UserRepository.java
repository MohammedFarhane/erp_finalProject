package be.technifutur.erp_finalproject.repositories;

import be.technifutur.erp_finalproject.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);
}
