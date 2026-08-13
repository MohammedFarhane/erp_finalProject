package be.technifutur.erp_finalproject.entities;

import be.technifutur.erp_finalproject.enums.QuoteState;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
public class Quote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Setter
    private QuoteState state;

    @Column(nullable = false)
    @Setter
    private LocalDate quoteDate;

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

    @OneToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    @Setter
    private Billing billing;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ToString.Exclude
    @Setter
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ToString.Exclude
    @Setter
    private User user;
}
