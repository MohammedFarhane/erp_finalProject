package be.technifutur.erp_finalproject.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(uniqueConstraints =
@UniqueConstraint(columnNames = {"year", "prefix"}))
@NoArgsConstructor
@Getter
public class ReferenceCounter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int year;

    @Column(length = 10)
    private String prefix;

    @Setter
    private int lastNumber;

    public ReferenceCounter(int year, String prefix, int lastNumber) {
        this.year = year;
        this.prefix = prefix;
        this.lastNumber = lastNumber;
    }
}
