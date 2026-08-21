package be.technifutur.erp_finalproject.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
public class BillingLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Setter
    private int quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    @Setter
    private BigDecimal unitPrice;

    @Column(nullable = false)
    @Setter
    private double tvaRate;

    @Column(nullable = false)
    @Setter
    private BigDecimal tvaAmount;

    @Column(nullable = false, precision = 10, scale = 2)
    @Setter
    private BigDecimal totalLinePrice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ToString.Exclude
    @Setter
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ToString.Exclude
    @Setter
    private Billing billing;

    public BillingLine(int quantity, BigDecimal unitPrice, double tvaRate, BigDecimal tvaAmount,
                       BigDecimal totalLinePrice, Product product) {
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.tvaRate = tvaRate;
        this.tvaAmount = tvaAmount;
        this.totalLinePrice = totalLinePrice;
        this.product = product;
    }
}
