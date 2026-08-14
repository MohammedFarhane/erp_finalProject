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
}
