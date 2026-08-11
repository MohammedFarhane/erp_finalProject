package be.technifutur.erp_finalproject.entities;

import jakarta.persistence.*;
import lombok.*;


@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Getter
public class Address {

    @Column(nullable = false, length = 100)
    private String street;

    @Column(nullable = false)
    private String number;

    @Column(nullable = false, length = 100)
    private String postalCode;

    @Column(nullable = false, length = 100)
    private String locality;
}
