package be.technifutur.erp_finalproject.entities;

import be.technifutur.erp_finalproject.enums.BillingState;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.Date;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = false, of = {})
@Getter
public class Billing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Setter
    private Date billingDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'BROUILLON'")
    @Setter
    private BillingState state;

    @Column
    @Setter
    private Double discount;

    @Column(nullable = false)
    @Setter
    private Double subTotal;

    @Column(nullable = false)
    @Setter
    private Double amountTva;

    @Column(nullable = false)
    @Setter
    private Double totalPrice;
}
