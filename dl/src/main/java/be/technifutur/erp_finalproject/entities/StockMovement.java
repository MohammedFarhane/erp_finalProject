package be.technifutur.erp_finalproject.entities;

import be.technifutur.erp_finalproject.enums.MovementType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Setter
    private MovementType type;

    @Column(nullable = false)
    @Setter
    private int quantity;

    @Column(nullable = false)
    @Setter
    private LocalDateTime movementDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ToString.Exclude
    @Setter
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    @Setter
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ToString.Exclude
    @Setter
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    @Setter
    private Billing billing;
}
