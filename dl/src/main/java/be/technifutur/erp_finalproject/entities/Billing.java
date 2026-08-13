package be.technifutur.erp_finalproject.entities;

import be.technifutur.erp_finalproject.enums.BillingState;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
public class Billing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Setter
    private LocalDate billingDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Setter
    private BillingState state;

    @Column
    @Setter
    private Double discount;

    @Column(nullable = false)
    @Setter
    private Double subTotal;

    @Column(nullable = false)
    @Setter
    private Double amountTva;

    @Column(nullable = false)
    @Setter
    private Double totalPrice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ToString.Exclude
    @Setter
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ToString.Exclude
    @Setter
    private Client client;
}
