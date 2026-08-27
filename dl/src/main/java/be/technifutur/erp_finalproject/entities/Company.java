package be.technifutur.erp_finalproject.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    @Setter
    private String name;

    @Column(nullable = false, length = 100)
    @Setter
    private String tvaNumber;

    @Column(nullable = false, length = 100)
    @Setter
    private String iban;

    @Column(nullable = false, length = 100)
    @Setter
    private String email;

    @Column(nullable = false, length = 100)
    @Setter
    private String phone;

    @Embedded
    @Setter
    private Address address;

    public Company(String name, String tvaNumber, String iban, String email, String phone, Address address) {
        this.name = name;
        this.tvaNumber = tvaNumber;
        this.iban = iban;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }
}
