package be.technifutur.erp_finalproject.entities;

import be.technifutur.erp_finalproject.enums.AddressType;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "email")
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

    @Column(nullable = false, length = 100)
    @Setter
    private String phone;

    @Column(nullable = false)
    @Setter
    private boolean archived;

    //Le client possède plusieurs adresses
    @ElementCollection
    @CollectionTable(
            name = "client_address",
            joinColumns = @JoinColumn(name = "client_id")
    )
    @ToString.Exclude
    @Setter
    private Set<TypeAddress> addresses = new HashSet<>();

    public Client(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public Address getBillingAddress() {
        return firstOfType(AddressType.FACTURATION)
                .or(() -> firstOfType(AddressType.LIVRAISON))
                .map(TypeAddress::getAddress)
                .orElse(null);
    }

    private Optional<TypeAddress> firstOfType(AddressType type) {
        return addresses.stream().filter(a -> a.getType() == type).findFirst();
    }
}