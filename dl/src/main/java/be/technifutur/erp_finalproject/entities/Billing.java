package be.technifutur.erp_finalproject.entities;

import be.technifutur.erp_finalproject.enums.BillingState;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
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

    @Column(nullable = false, unique = true, length = 30)
    private String reference;

    @Column(nullable = false)
    @Setter
    private LocalDate billingDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Setter
    private BillingState state;

    @Setter
    @Column(precision = 10, scale = 2)
    private BigDecimal discount;

    @Column(nullable = false, precision = 10, scale = 2)
    @Setter
    private BigDecimal subTotal;

    @Column(nullable = false, precision = 10, scale = 2)
    @Setter
    private BigDecimal amountTva;

    @Column(nullable = false, precision = 10, scale = 2)
    @Setter
    private BigDecimal totalPrice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ToString.Exclude
    @Setter
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ToString.Exclude
    @Setter
    private Client client;

    public Billing(String reference, LocalDate billingDate, BigDecimal discount,
                   BigDecimal subTotal, BigDecimal amountTva, BigDecimal totalPrice, User user, Client client) {
        this.reference = reference;
        this.billingDate = billingDate;
        this.state = BillingState.BROUILLON;
        this.discount = discount;
        this.subTotal = subTotal;
        this.amountTva = amountTva;
        this.totalPrice = totalPrice;
        this.user = user;
        this.client = client;
    }
}
