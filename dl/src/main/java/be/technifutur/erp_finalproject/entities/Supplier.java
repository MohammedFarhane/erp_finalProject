package be.technifutur.erp_finalproject.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
public class Supplier {

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

    //Le fournisseur n'a qu'une seule adresse
    @Embedded
    @Setter
    private Address address;
}
