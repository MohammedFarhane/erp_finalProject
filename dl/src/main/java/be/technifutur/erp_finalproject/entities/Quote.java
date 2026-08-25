package be.technifutur.erp_finalproject.entities;

import be.technifutur.erp_finalproject.enums.QuoteState;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
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

    @Setter
    @Column(nullable = false, unique = true, length = 30)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Setter
    private QuoteState state;

    @Column(nullable = false)
    @Setter
    private LocalDate quoteDate;

    @Column(nullable = false, precision = 10, scale = 2)
    @Setter
    private BigDecimal subTotal;

    @Column(precision = 10, scale = 2)
    @Setter
    private BigDecimal discount;

    @Column(nullable = false, precision = 10, scale = 2)
    @Setter
    private BigDecimal amountTva;

    @Column(nullable = false, precision = 10, scale = 2)
    @Setter
    private BigDecimal totalPrice;

    @Column(nullable = false)
    @Setter
    private LocalDate expirationDate;

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

    public Quote(String reference, LocalDate quoteDate, BigDecimal subTotal, BigDecimal discount,
                 BigDecimal amountTva, BigDecimal totalPrice, LocalDate expirationDate, Client client, User user) {
        this.reference = reference;
        this.quoteDate = quoteDate;
        this.subTotal = subTotal;
        this.discount = discount;
        this.amountTva = amountTva;
        this.totalPrice = totalPrice;
        this.expirationDate = expirationDate;
        this.client = client;
        this.user = user;
        this.state = QuoteState.BROUILLON;
    }
}
