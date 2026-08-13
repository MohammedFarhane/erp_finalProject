package be.technifutur.erp_finalproject.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
public class QuoteLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Setter
    private int quantity;

    @Column(nullable = false)
    @Setter
    private Double unitPrice;

    @Column(nullable = false)
    @Setter
    private Double tvaAmount;

    @Column(nullable = false)
    @Setter
    private Double totalLinePrice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ToString.Exclude
    @Setter
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ToString.Exclude
    @Setter
    private Quote quote;
}
