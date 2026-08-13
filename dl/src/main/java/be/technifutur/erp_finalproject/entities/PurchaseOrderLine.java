package be.technifutur.erp_finalproject.entities;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(nullable = false)
    @Setter
    private Double unitPrice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ToString.Exclude
    @Setter
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ToString.Exclude
    @Setter
    private PurchaseOrder purchaseOrder;
}
