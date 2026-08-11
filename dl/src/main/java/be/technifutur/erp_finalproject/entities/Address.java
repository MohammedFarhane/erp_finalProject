package be.technifutur.erp_finalproject.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = false, of = {})
@Getter
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    @Setter
    private String street;

    @Column(nullable = false)
    @Setter
    private String number;

    @Column(nullable = false, length = 100)
    @Setter
    private String postalCode;

    @Column(nullable = false, length = 100)
    @Setter
    private String locality;

}
