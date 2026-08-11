package be.technifutur.erp_finalproject.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor @AllArgsConstructor
@ToString @EqualsAndHashCode(callSuper = false, of = {})
@Getter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    @Setter
    private String name;

    @Column(length = 250)
    @Setter
    private String description;

    @Column(nullable = false)
    @Setter
    private Double purchasePrice;

    @Column(nullable = false)
    @Setter
    private Double sellingPrice;

    @Column(nullable = false)
    @Setter
    private double tva_rate;

    @Column(nullable = false)
    @Setter
    private int stockQuantity;

    @Column(nullable = false)
    @Setter
    private int minStockQuantity;
}
