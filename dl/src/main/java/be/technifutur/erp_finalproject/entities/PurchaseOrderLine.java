package be.technifutur.erp_finalproject.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
public class PurchaseOrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Setter
    private int quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    @Setter
    private BigDecimal unitPrice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ToString.Exclude
    @Setter
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ToString.Exclude
    @Setter
    private PurchaseOrder purchaseOrder;

    public PurchaseOrderLine(int quantity, BigDecimal purchasePrice, Product product) {
        this.quantity = quantity;
        this.unitPrice = purchasePrice;
        this.product = product;
    }
}
