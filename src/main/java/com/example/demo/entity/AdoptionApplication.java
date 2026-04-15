package com.example.demo.entity;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
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
@Table(name = "adoption_applications")
public class AdoptionApplication extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Hakijan nimi on pakollinen")
    @Size(min = 2, max = 80, message = "Hakijan nimen pituus 2-80 merkkiä")
    private String applicantName;

    @Email(message = "Sähköpostin tulee olla kelvollinen")
    @NotBlank(message = "Sähköposti on pakollinen")
    @Size(max = 120, message = "Sähköpostin pituus saa olla enintään 120 merkkiä")
    private String applicantEmail;

    @NotNull(message = "Hakupäivä on pakollinen")
    @PastOrPresent(message = "Hakupäivä ei voi olla tulevaisuudessa")
    private LocalDate applicationDate;

    @NotNull(message = "Status on pakollinen")
    @Enumerated(EnumType.STRING)
    private AdoptionStatus status;

    @NotBlank(message = "Viesti on pakollinen")
    @Size(min = 10, max = 800, message = "Viestin pituus 10-800 merkkiä")
    private String message;

    @ManyToOne
    @JoinColumn(name = "cat_id", nullable = false)
    private Cat cat;
}
