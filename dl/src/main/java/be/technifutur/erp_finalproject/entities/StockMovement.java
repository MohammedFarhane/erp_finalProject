package be.technifutur.erp_finalproject.entities;

import be.technifutur.erp_finalproject.enums.MovementType;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = false, of = {})
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
    private Date movementDate;
}
