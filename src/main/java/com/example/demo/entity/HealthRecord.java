package com.example.demo.entity;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@Table(name = "health_records")
public class HealthRecord extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Viimeisin tarkastus on pakollinen")
    @PastOrPresent(message = "Viimeisin tarkastus ei voi olla tulevaisuudessa")
    private LocalDate lastCheckup;

    @NotBlank(message = "Eläinlääkärin nimi on pakollinen")
    @Size(min = 2, max = 80, message = "Eläinlääkärin nimen pituus 2-80 merkkiä")
    private String veterinarian;

    @NotNull(message = "Paino on pakollinen")
    @DecimalMin(value = "0.1", message = "Painon täytyy olla vähintään 0.1 kg")
    @DecimalMax(value = "20.0", message = "Paino saa olla enintään 20.0 kg")
    private Double weight;

    @NotBlank(message = "Rokotukset-kenttä on pakollinen")
    @Size(min = 3, max = 300, message = "Rokotukset-kentän pituus 3-300 merkkiä")
    private String vaccinations;

    @Size(max = 500, message = "Huomiot saa olla enintään 500 merkkiä")
    private String notes;

    @OneToOne
    @JoinColumn(name = "cat_id", nullable = false, unique = true)
    private Cat cat;
}
