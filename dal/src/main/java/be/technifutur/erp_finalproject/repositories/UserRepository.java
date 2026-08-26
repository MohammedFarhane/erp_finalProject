package be.technifutur.erp_finalproject.repositories;

import be.technifutur.erp_finalproject.entities.User;
import be.technifutur.erp_finalproject.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {

    @Query("""
            select u from User u
            where u.archived = false
                and (:namePattern is null or lower(u.name) like :namePattern )
                and (:emailPattern is null or lower(u.email) like :emailPattern )
            """)
    Page<User> search (
            @Param("namePattern") String name,
            @Param("emailPattern") String email,
            UserRole role,
            Pageable pageable
    );

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndArchivedFalse(String email);

    Optional<User> findByIdAndArchivedFalse(Long id);

    boolean existsByEmail(String email);

    @Query("""
            select count(u) from User u where u.role = :role and u.archived = false
           """)
    Long countByRoleAndArchivedFalse(UserRole role);
}
