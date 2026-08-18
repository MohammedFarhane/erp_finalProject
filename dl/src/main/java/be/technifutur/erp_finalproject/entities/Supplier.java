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

    @Column(nullable = false, length = 100)
    @Setter
    private String phone;

    @Column(nullable = false)
    @Setter
    private boolean archived;

    //Le fournisseur n'a qu'une seule adresse
    @Embedded
    @Setter
    private Address address;

    public Supplier(String name, String email, String phone, Address address) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }
}
