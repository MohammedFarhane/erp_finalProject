package be.technifutur.erp_finalproject.entities;

import be.technifutur.erp_finalproject.enums.AddressType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor @AllArgsConstructor
public class TypeAddress {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AddressType type;

    @Embedded
    private Address address;
}