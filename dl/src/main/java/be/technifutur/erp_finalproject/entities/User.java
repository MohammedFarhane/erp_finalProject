package be.technifutur.erp_finalproject.entities;

import be.technifutur.erp_finalproject.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "user_")
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    @Setter
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    @Setter
    private String email;

    @Column(nullable = false, length = 100)
    @Setter
    @ToString.Exclude
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Setter
    private UserRole role;

    @Column(nullable = false)
    @Setter
    private boolean archived;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_client",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "client_id")
    )
    @ToString.Exclude
    @Setter
    private Set<Client> clients = new HashSet<>();

    public User(String name, String email, String password, UserRole role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public User(Long id, String name, String email, String password, UserRole role, boolean archived) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.archived = archived;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return getEmail();
    }
}
