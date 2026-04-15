package com.example.demo.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "tags")
public class Tag extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tagin nimi on pakollinen")
    @Size(min = 2, max = 40, message = "Tagin nimen pituus 2-40 merkkiä")
    private String name;

    @NotBlank(message = "Tagin kategoria on pakollinen")
    @Size(min = 2, max = 40, message = "Tagin kategorian pituus 2-40 merkkiä")
    private String category;

    @Size(max = 200, message = "Tagin kuvaus saa olla enintään 200 merkkiä")
    private String description;

    @NotBlank(message = "Tagin väri on pakollinen")
    @Size(max = 20, message = "Tagin värin pituus saa olla enintään 20 merkkiä")
    private String colorCode;

    @NotBlank(message = "Tagin ikoni on pakollinen")
    @Size(max = 40, message = "Tagin ikonin pituus saa olla enintään 40 merkkiä")
    private String icon;

    @ManyToMany(mappedBy = "tags", fetch = FetchType.EAGER)
    private Set<Cat> cats = new HashSet<>();
}
