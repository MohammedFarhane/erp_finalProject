package be.technifutur.erp_finalproject.entities;

import be.technifutur.erp_finalproject.enums.PurchaseOrderState;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.Date;

@Entity
@NoArgsConstructor @AllArgsConstructor
@ToString @EqualsAndHashCode(callSuper = false, of = {})
@Getter
public class PurchaseOrder {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Setter
    private Date date;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'EN_ATTENTE'")
    @Setter
    private PurchaseOrderState state;

    @Column(nullable = false)
    @Setter
    private Double totalPrice;
}
