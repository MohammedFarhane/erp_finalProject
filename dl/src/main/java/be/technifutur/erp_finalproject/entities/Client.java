package be.technifutur.erp_finalproject.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = false, of = {})
@Getter
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    @Setter
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    @Setter
    private String email;

    @Column(nullable = false, unique = true, length = 100)
    @Setter
    private String phone;

    //Le client possède plusieurs adresses
    @ElementCollection
    @CollectionTable(
            name = "client_address",
            joinColumns = @JoinColumn(name = "client_id")
    )
    @ToString.Exclude
    @Setter
    private Set<Address> addresses = new HashSet<>();
}
