package be.technifutur.erp_finalproject.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@NoArgsConstructor @AllArgsConstructor
@ToString
@Getter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String reference;

    @Column(nullable = false, length = 100)
    @Setter
    private String name;

    @Column(length = 250)
    @Setter
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    @Setter
    private BigDecimal purchasePrice;

    @Column(nullable = false, precision = 10, scale = 2)
    @Setter
    private BigDecimal sellingPrice;

    @Column(nullable = false)
    @Setter
    private double tvaRate;

    @Column(nullable = false)
    @Setter
    private int minStockQuantity;

    @Setter
    private boolean archived;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ToString.Exclude
    @Setter
    private Category category;

    public Product(String reference, String name, String description, BigDecimal purchasePrice, BigDecimal sellingPrice, double tvaRate, int minStockQuantity, Category category) {
        this.reference = reference;
        this.name = name;
        this.description = description;
        this.purchasePrice = purchasePrice;
        this.sellingPrice = sellingPrice;
        this.tvaRate = tvaRate;
        this.minStockQuantity = minStockQuantity;
        this.category = category;
    }

}
