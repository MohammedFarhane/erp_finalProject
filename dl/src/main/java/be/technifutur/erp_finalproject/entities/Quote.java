package be.technifutur.erp_finalproject.entities;

import be.technifutur.erp_finalproject.enums.QuoteState;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = false, of = {})
@Getter
public class Quote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ColumnDefault("'EN_ATTENTE'")
    @Setter
    private QuoteState state;

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
