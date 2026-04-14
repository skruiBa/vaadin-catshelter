package com.example.demo.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cats")
public class Cat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nimi ei voi olla tyhjä")
    @Size(min = 2, max = 60, message = "Nimen pituus 2-60 merkkiä")
    private String name;

    @NotBlank(message = "Rotu on pakollinen")
    @Size(min = 2, max = 60, message = "Rodun pituus 2-60 merkkiä")
    private String breed;

    @NotNull(message = "Syntymäaika on pakollinen")
    @PastOrPresent(message = "Syntymäaika ei voi olla tulevaisuudessa")
    private LocalDate birthDate;

    @NotNull(message = "Sukupuoli on pakollinen")
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @NotBlank(message = "Kuvan URL on pakollinen")
    @Size(max = 255, message = "Kuvan URL saa olla enintään 255 merkkiä")
    private String imageUrl;

    @NotBlank(message = "Väri on pakollinen")
    @Size(max = 40, message = "Värin pituus saa olla enintään 40 merkkiä")
    private String color;

    @NotNull(message = "Paino on pakollinen")
    @DecimalMin(value = "0.1", message = "Painon täytyy olla vähintään 0.1 kg")
    @DecimalMax(value = "20.0", message = "Paino saa olla enintään 20.0 kg")
    private Double weight;

    @NotNull(message = "Saapumispäivä on pakollinen")
    @PastOrPresent(message = "Saapumispäivä ei voi olla tulevaisuudessa")
    private LocalDate arrivalDate;

    @Size(max = 500, message = "Kuvaus saa olla enintään 500 merkkiä")
    private String description;

    @OneToOne(mappedBy = "cat", cascade = CascadeType.ALL, orphanRemoval = true)
    private HealthRecord healthRecord;

    @OneToMany(mappedBy = "cat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AdoptionApplication> adoptionApplications = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "cat_tags", joinColumns = @JoinColumn(name = "cat_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();

}
