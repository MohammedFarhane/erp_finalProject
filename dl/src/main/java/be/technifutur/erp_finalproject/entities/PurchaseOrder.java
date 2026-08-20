package be.technifutur.erp_finalproject.entities;

import be.technifutur.erp_finalproject.enums.PurchaseOrderState;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@NoArgsConstructor @AllArgsConstructor
@ToString
@Getter
public class PurchaseOrder {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String reference;

    @Column(nullable = false)
    @Setter
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Setter
    private PurchaseOrderState state;

    @Column(nullable = false, precision = 10, scale = 2)
    @Setter
    private BigDecimal totalPrice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ToString.Exclude
    @Setter
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ToString.Exclude
    @Setter
    private User user;

    public PurchaseOrder(String reference, LocalDate date, BigDecimal totalPrice,
                         Supplier supplier, User user) {
        this.reference = reference;
        this.date = date;
        this.totalPrice = totalPrice;
        this.supplier = supplier;
        this.state = PurchaseOrderState.EN_ATTENTE;
        this.user = user;
    }
}
