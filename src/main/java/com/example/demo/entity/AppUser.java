package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Audited
@Table(name = "app_users")
public class AppUser extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Käyttäjänimi on pakollinen")
    @Size(min = 3, max = 50, message = "Käyttäjänimen pituus 3-50 merkkiä")
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank(message = "Sähköposti on pakollinen")
    @Email(message = "Sähköpostin tulee olla kelvollinen")
    @Size(max = 120)
    @Column(unique = true, nullable = false)
    private String email;

    /** BCrypt-hashattu salasana — ei koskaan tallenneta selväkielisenä */
    @NotBlank
    @NotAudited
    @Column(nullable = false)
    private String passwordHash;

    @NotBlank(message = "Etunimi on pakollinen")
    @Size(min = 1, max = 60)
    private String firstName;

    @NotBlank(message = "Sukunimi on pakollinen")
    @Size(min = 1, max = 60)
    private String lastName;

    /** Profiilikuvan polku/URL — valinnainen */
    @Size(max = 255)
    private String profileImagePath;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<Role> roles;

    private boolean enabled = true;
}
