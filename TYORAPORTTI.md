# Vaadin Web -harjoitustyö: Työraportti

**Sovellus:** CatShelter – kissatarhan hallintajärjestelmä
**Teknologiat:** Vaadin 24.6.6, Spring Boot 3.4.4, Java 21, H2 (dev) / PostgreSQL (tuotanto), Docker
**GitHub:** https://github.com/skruiBa/vaadin-catshelter

---

## A) Data, entiteetit ja CRUD

### A1 – CRUD kayttoliittymalta tietokantaan

Kaikille neljalle paaentiteetille (Cat, HealthRecord, AdoptionApplication, Tag) on toteutettu:

- Grid-listausnakyma
- Lisayslomake (Dialog + FormLayout)
- Muokkauslomake (sama dialog, edit-moodi)
- Poisto ConfirmDialogilla

CRUD-kutsut kulkevat: UI-nakyma → JpaRepository → tietokanta.

Tiedostot:

- `src/main/java/com/example/demo/ui/view/CatsView.java`
- `src/main/java/com/example/demo/ui/view/HealthRecordsView.java`
- `src/main/java/com/example/demo/ui/view/AdoptionApplicationsView.java`
- `src/main/java/com/example/demo/ui/view/TagsView.java`
- `src/main/java/com/example/demo/repository/CatRepository.java`
- `src/main/java/com/example/demo/repository/HealthRecordRepository.java`
- `src/main/java/com/example/demo/repository/AdoptionApplicationRepository.java`
- `src/main/java/com/example/demo/repository/TagRepository.java`

### A2 – Entiteetti 1: Cat

Kentat: name, breed, birthDate, gender, color, weight, arrivalDate, imageUrl, description.

Tiedosto: `src/main/java/com/example/demo/entity/Cat.java`

### A3 – Entiteetti 2 + 1:1-relaatio: HealthRecord

`Cat` sisaltaa `@OneToOne(cascade=ALL) HealthRecord healthRecord`.
UI-nakyvyys: HealthRecordsView-gridissa nakyy relaatiokissan nimi (`record.getCat().getName()`).

Tiedostot:

- `src/main/java/com/example/demo/entity/HealthRecord.java`
- `src/main/java/com/example/demo/entity/Cat.java`
- `src/main/java/com/example/demo/ui/view/HealthRecordsView.java`

### A4 – Entiteetti 3 + 1:N-relaatio: AdoptionApplication

`Cat` sisaltaa `@OneToMany(cascade=ALL) List<AdoptionApplication> adoptionApplications`.
UI-nakyvyys: AdoptionApplicationsView-gridissa nakyy kissan nimi relaatiosta.

Tiedostot:

- `src/main/java/com/example/demo/entity/AdoptionApplication.java`
- `src/main/java/com/example/demo/entity/Cat.java`
- `src/main/java/com/example/demo/ui/view/AdoptionApplicationsView.java`

### A5 – Entiteetti 4 + M:N-relaatio: Tag

`Cat` sisaltaa `@ManyToMany Set<Tag> tags` (liitostauluna `cat_tags`).
UI-nakyvyys: CatsView-gridissa tagit naytetaan badgeina; TagsView nayttaa kuhunkin tagiin liittyvien kissojen maaran.

Tiedostot:

- `src/main/java/com/example/demo/entity/Tag.java`
- `src/main/java/com/example/demo/entity/Cat.java`
- `src/main/java/com/example/demo/ui/view/CatsView.java`
- `src/main/java/com/example/demo/ui/view/TagsView.java`

### A6 – Vahintaan 5 validoitavaa kenttaa kaikille entiteeteille

Kaytetyt annotaatiot: `@NotBlank`, `@NotNull`, `@Size`, `@Email`, `@PastOrPresent`, `@DecimalMin`, `@DecimalMax`.

Esimerkit:

- **Cat:** name `@Size(min=2,max=60)`, birthDate `@PastOrPresent`, weight `@DecimalMin("0.1") @DecimalMax("20.0")`, gender `@NotNull`, arrivalDate `@NotNull`
- **HealthRecord:** lastCheckup `@NotNull`, veterinarian `@NotBlank @Size(min=2,max=80)`, weight `@DecimalMin`, vaccinations `@NotBlank`, notes `@Size(max=500)`
- **AdoptionApplication:** applicantName `@Size(min=2,max=80)`, applicantEmail `@Email @NotBlank`, applicationDate `@NotNull`, status `@NotNull`, message `@Size(min=10,max=800)`
- **Tag:** name `@NotBlank @Size(max=50)`, category `@NotBlank @Size(max=50)`, colorCode `@Size(max=20)`, icon `@Size(max=50)`, description `@Size(max=200)`
- **AppUser:** username `@Size(min=3,max=50)`, email `@Email @NotBlank`, passwordHash `@NotBlank`, firstName `@Size(max=50)`, lastName `@Size(max=50)`

Tiedostot: `src/main/java/com/example/demo/entity/` (Cat, HealthRecord, AdoptionApplication, Tag, AppUser)

---

## B) Suodattaminen (Criteria API)

Kaikki haku on toteutettu `CatSearchService`-luokassa JPA Criteria API:lla. Predikaatteja lisataan vain jos kayttaja on syottanyt ehdon.

Tiedostot:

- `src/main/java/com/example/demo/repository/CatSearchService.java`
- `src/main/java/com/example/demo/repository/CatSearchFilter.java`
- `src/main/java/com/example/demo/ui/view/CatSearchView.java`

### B1 – Suodatus usealla syotekentalla (>= 3)

Kentat: name (LIKE), breed (LIKE), color (LIKE), gender (tarkka tasmaytys), tagName (LIKE), tagCategory (tarkka), nameOrBreed (LIKE OR). Kayttaja voi tayttaa mita tahansa naista.

### B2 – Paivamaaravalin suodatus

`arrivalFrom` ja `arrivalTo` -> `cb.between(cat.get("arrivalDate"), f.getArrivalFrom(), f.getArrivalTo())`.
Toimii myos yksittaisella rajalla: `>=` tai `<=` erikseen.

Koodi `CatSearchService.java` rivit 69–76.

### B3 – JOIN-suodatus relaatioentiteetin perusteella

`Join<Cat, Tag> tagJoin = cat.join("tags", JoinType.LEFT)` — haku tagin nimen perusteella (`tagName`-kentta).

Koodi `CatSearchService.java` rivi 42, rivit 79–82.

### B4 – Suodatus relaatioentiteetin ominaisuudella

Sama JOIN-taulun kautta suodatus `tag.category`-kentan perusteella: `cb.equal(cb.lower(tagJoin.get("category")), ...)`.

Koodi `CatSearchService.java` rivit 85–88.

### B5 – Monimutkainen (X OR Y) AND Z

`nameOrBreed`-kentan arvo muodostaa: `(name LIKE x OR breed LIKE x)`, joka liitetaan `AND`-logiikalla muihin predikaatteihin.

Koodi `CatSearchService.java` rivit 91–95.

---

## C) Tyylit ja ulkoasu

### C1 – Globaalit tyylit

`styles.css` ylikirjoittaa Lumo-muuttujat:

- Fontti: `--lumo-font-family: 'Nunito', sans-serif` (Google Fonts)
- Varipaletti: `--lumo-primary-color: #2d2876`, `--cs-accent`, `--cs-secondary`, `--lumo-base-color: #eff1fb`
- Kaarevuus: `--lumo-border-radius-s/m/l` (8/12/18px)
- Box-shadow: `--lumo-box-shadow-xs/s/m/l` (sinisavyiset varjot)

Tiedosto: `src/main/frontend/themes/catshelter/styles.css`

### C2 – Komponenttien tyylimuokkaus

a) `addClassName`: `CatsView.java` rivi 69 (`addClassName("cats-view")`), `DashboardView.java` rivit 37, 94 (`"dashboard-title"`, `"stat-card"`)

b) `getStyle().set()`: `DashboardView.java` rivit 32–33 (`padding`), 99–102 (kortti-tyyli); `MainLayout.java` rivit 31–32 (title-fontti), 124–126 (footer)

c) `addThemeVariants`/`setThemeVariants`: `MainLayout.java` rivi 54 (`LUMO_TERTIARY`, `LUMO_SMALL`), `CatsView.java` (napit `LUMO_PRIMARY`, `LUMO_ERROR`), `RichTextNotesView.java` rivit 66–67 (`LUMO_PRIMARY`, `LUMO_TERTIARY`)

### C3 – Nakymakohtainen CSS

`.cats-view`-luokka kohdistaa tyylit vain CatsView-nakymaan, useampaan Vaadin-komponenttiin samassa nakymassa:

```css
.cats-view h2 {
    color: var(--lumo-primary-color);
}
.cats-view vaadin-grid {
    border-radius: ...;
    box-shadow: ...;
}
.cats-view vaadin-button[theme~='primary'] {
    box-shadow: ...;
}
.cats-view vaadin-text-field,
.cats-view vaadin-combo-box,
.cats-view vaadin-date-picker {
    border-radius: ...;
}
```

Luokka lisataan nakymaan: `CatsView.java` rivi 69 `addClassName("cats-view")`.

Tiedostot: `src/main/frontend/themes/catshelter/styles.css` rivit 71–89, `CatsView.java` rivi 69.

### C4 – LumoUtility-luokkia vahintaan 5

`DashboardView.java` kayttaa: `LumoUtility.TextColor.SECONDARY`, `LumoUtility.Margin.Top.NONE`, `LumoUtility.Margin.Vertical.LARGE`, `LumoUtility.Background.CONTRAST_5`, `LumoUtility.Padding.MEDIUM`, `LumoUtility.BorderRadius.LARGE`, `LumoUtility.FontSize.LARGE`, `LumoUtility.BoxShadow.SMALL`, `LumoUtility.FontSize.SMALL`, `LumoUtility.TextColor.BODY` — yhteensa yli 7 eri luokkaa.

### C5 – Hover, focus ja transition

`styles.css` rivit 50–65: `.stat-card` — `transition: transform 0.2s ease, box-shadow 0.2s ease`, hover zoomaa 3% (`scale(1.03)`), focus-within nayttaa primary-varin outline-kehyksen.

`components/vaadin-button.css`: kaikki vaadin-napit saavat `transition: transform 0.15s ease, ...`, hover nostaa nappia (`translateY(-2px)`), `:focus-visible` nayttaa outline-kehyksen.

---

## D) Ulkoasu ja rakenne (SPA)

### D1 – SPA-rakenne: MainLayout + AppLayout + @Route

`MainLayout extends AppLayout`. Kaikki nakymat kaytt `@Route(value = "...", layout = MainLayout.class)`.

Tiedosto: `src/main/java/com/example/demo/ui/MainLayout.java`

### D2 – Vahintaan 3 erityyppista nakymaa

- **DashboardView** (`/`): tilastokortteja FlexLayoutissa, info-bokseja — tilastopohjainen etusivu
- **CatsView** (`/kissat`): Grid + Dialogi-lomake + CSV-toiminnot — CRUD-nakyma
- **CatSearchView** (`/haku`): hakukenttia FormLayoutissa + tulosGrid — hakunakyma
- **RichTextNotesView** (`/muistiinpanot`): Quill-editori + kielivalitsin — editori-nakyma

### D3 – Header

`MainLayout.createHeader()`: `DrawerToggle`, sovelluksen nimi (`H1 "CatShelter"`), kayttajan nimi (`Span` + `VaadinIcon.USER`), kirjaudu ulos/sisaan-nappi.

Tiedosto: `src/main/java/com/example/demo/ui/MainLayout.java` rivit 29–66.

### D4 – Navigointi: linkit, ikonit, aktiivinen sivu

`SideNav` sisaltaa `SideNavItem`-elementit uniikeilla ikoneilla jokaiselle nakymalle: `HOME`, `STAR`, `HEART`, `FILE_TEXT`, `TAG`, `SEARCH`, `EDIT`, `TIME_BACKWARD`. Vaadin `SideNav` korostaa aktiivisen sivun automaattisesti (`vaadin-side-nav-item[active]` on tyylitelty `styles.css`:ssa).

Tiedosto: `src/main/java/com/example/demo/ui/MainLayout.java` rivit 68–93.

### D5 – Footer

`MainLayout.createFooter()`: tekijan nimi `© 2026 CatShelter`, kuvaus `Kissatarhan hallintajärjestelmä`. Erottuu visuaalisesti `border-top: 1px solid var(--lumo-contrast-10pct)` ja lisatyylilla `styles.css`:ssa (`background`, `border-top: 2px solid`). Kiinnitetty drawerin alaosaan AppLayoutin rakennetta kayttaen.

Tiedosto: `src/main/java/com/example/demo/ui/MainLayout.java` rivit 113–127.

---

## E) Autentikointi ja tietoturva

### E1 – Spring Security, kayttajat, roolit, hash-salasanat

`SecurityConfig extends VaadinWebSecurity`, salasanat hashataan `BCryptPasswordEncoder`-luokalla. Kayttajia ei voi lukea tietokannasta selkokielisena — `AppUser.passwordHash` sisaltaa vain BCrypt-tiivisteen.

`DataInitializer` luo kayttajat kaytnnistyksen yhteydessa (jos eivat vielä olemassa):

- admin / admin123 → ROLE_ADMIN
- superuser / super123 → ROLE_SUPER
- user / user123 → ROLE_USER

Tiedostot:

- `src/main/java/com/example/demo/SecurityConfig.java`
- `src/main/java/com/example/demo/security/AppUserDetailsService.java`
- `src/main/java/com/example/demo/entity/AppUser.java`
- `src/main/java/com/example/demo/DataInitializer.java`

### E2 – Roolipohjaiset sivurajaukset

| Sivu                                                                              | Rajaus                                       |
| --------------------------------------------------------------------------------- | -------------------------------------------- |
| DashboardView (`/`)                                                               | `@AnonymousAllowed` – kaikki                 |
| CatsView, AdoptionApplicationsView, CatSearchView, RichTextNotesView, HistoryView | `@PermitAll` – kirjautuneet                  |
| HealthRecordsView                                                                 | `@RolesAllowed({"ROLE_SUPER", "ROLE_USER"})` |
| TagsView                                                                          | `@RolesAllowed("ROLE_ADMIN")`                |

### E3 – Rekisteroitymissivu

Koko rekisterointiputki: kayttajanimi, email, salasana (x2), etunimi, sukunimi, profiilikuvan lataus.

Tiedosto: `src/main/java/com/example/demo/ui/view/RegisterView.java`

### E4 – Kustomoitu virheviesti kaytto-oikeuspuutteeseen

`AccessDeniedView` naytetaan kun kayttajalla ei ole riittavia oikeuksia.

Tiedosto: `src/main/java/com/example/demo/ui/view/AccessDeniedView.java`

### E5 – Oman kuvan lisaaminen kayttajalle

Rekisteroitymislomakkeessa `Upload`-komponentti. `ProfileImageStorageService` validoi tiedostokoon (maks 2 MB), tarkistaa extension-whitelist (png/jpg/jpeg/gif/webp) ja sanitoi tiedostonimen ennen tallennusta.

Tiedostot:

- `src/main/java/com/example/demo/ui/view/RegisterView.java`
- `src/main/java/com/example/demo/service/ProfileImageStorageService.java`

### E6 – OAuth2-autentikointi (GitHub)

`SecurityConfig.java` aktivoi `http.oauth2Login()` ehdollisesti, jos `spring.security.oauth2.client.registration.github.client-id` on maaritetty ymparistomuuttujissa. `LoginView` nayttaa GitHub OAuth -napin dynaamisesti, kun konfiguraatio on paikalla.

Tiedostot:

- `src/main/java/com/example/demo/SecurityConfig.java`
- `src/main/java/com/example/demo/ui/view/LoginView.java`

---

## F) Muut toiminnallisuudet

### F1 – Julkaisu GitHubiin

Repo julkaistu GitHubiin (ks. raportin ylaosa).

### F2 – Vaadin Server Push

`@Push`-annotaatio `AppShell.java`:ssa mahdollistaa palvelinpushin kaikille nakymille.

Tiedosto: `src/main/java/com/example/demo/AppShell.java`

### F3 – Lokalisointi

`RichTextNotesView` sisaltaa FI/EN-kielivalitsimen (`ComboBox`). Kielivalinta paivittaa otsikot, kuvaukset, nappien tekstit, placeholder-tekstin ja ilmoitusviestit.

Tiedosto: `src/main/java/com/example/demo/ui/view/RichTextNotesView.java`

### F4 – Docker image + Dockerfile

`Dockerfile` rakentaa ajokelpoisen Spring Boot -imagen. Sovellus voidaan kaynnistaa `docker build` + `docker run` -komennoilla.

Tiedosto: `Dockerfile`

### F5 – docker-compose (db + app)

`docker-compose.yml` kaynnistaa PostgreSQL 16 -tietokannan ja Vaadin-sovelluksen. Sovellus kayttaa `application-docker.properties`-profiilia (`spring.profiles.active=docker`), joka osoittaa PostgreSQL-kontaineriin.

Tiedosto: `docker-compose.yml`

### F6 – Sahkopostin lahetys yllapitokayttajalle uuden kayttajan luonnista

`RegistrationNotificationService.notifyAdminNewUser(user)` kutsutaan `RegisterView`-luokasta onnistuneen rekisteroinnin jalkeen. Palvelu lahettaa sahkopostin `app.admin.email`-osoitteeseen. Jos `JavaMailSender` ei ole konfiguroitu (kehitysymparisto), palvelu kirjoittaa viestin lokiin.

Tiedostot:

- `src/main/java/com/example/demo/service/RegistrationNotificationService.java`
- `src/main/java/com/example/demo/ui/view/RegisterView.java`

### F7 – Salasanan vaihto sahkopostin avulla

Tyo: `ForgotPasswordView` → `PasswordResetService.requestPasswordReset(email)` → lahettaa sahkopostin reset-tokenin sisaltavalla linkilla (tai kirjaa lokiin kehitysymparistossa) → `ResetPasswordView` validoi tokenin ja paivittaa salasanan.

`PasswordResetToken`-entiteetti tallentaa tokenin ja vanhentumisajan (30 min).

Tiedostot:

- `src/main/java/com/example/demo/entity/PasswordResetToken.java`
- `src/main/java/com/example/demo/repository/PasswordResetTokenRepository.java`
- `src/main/java/com/example/demo/service/PasswordResetService.java`
- `src/main/java/com/example/demo/ui/view/ForgotPasswordView.java`
- `src/main/java/com/example/demo/ui/view/ResetPasswordView.java`

### F8 – Tiedoston lataus ja tallennus

`RegisterView`: kayttaja lataa profiilikuvan `Upload`-komponentilla. `ProfileImageStorageService` tallentaa tiedoston palvelimelle (`/uploads/profile-images/`).

Tiedostot:

- `src/main/java/com/example/demo/ui/view/RegisterView.java`
- `src/main/java/com/example/demo/service/ProfileImageStorageService.java`

### F9 – CSV-datan tuonti ja vienti

`CatsView` sisaltaa:

- **Vienti:** `Anchor`-komponentti, joka luo `StreamResource` CSV-muodossa kaikista kissoista (nimi, rotu, vari, paino, saapumispaiva, tagit).
- **Tuonti:** `Upload`-komponentti, joka parsii CSV-tiedoston rivi rivilta ja tallentaa kissat tietokantaan.

Tiedosto: `src/main/java/com/example/demo/ui/view/CatsView.java`

### F10 – Spring Auditing

`@EnableJpaAuditing` on kaytetty `DemoApplication.java`:ssa. Kaikkien entiteettien ylaluokka `AuditableEntity` sisaltaa `@CreatedDate Instant createdAt` ja `@LastModifiedDate Instant updatedAt`, jotka Spring Data paivittaa automaattisesti.

Tiedostot:

- `src/main/java/com/example/demo/DemoApplication.java`
- `src/main/java/com/example/demo/entity/AuditableEntity.java`

### F11 – Historiatiedot jokaisesta entiteetin muutoksesta

Hibernate Envers (`hibernate-envers` pom.xml:ssa) tallentaa revision jokaisen `@Audited`-merkityn entiteetin create/update/delete-operaatiosta. Merkityt entiteetit: `Cat`, `HealthRecord`, `AdoptionApplication`, `Tag`, `AppUser`.

### F12 – Historiatiedon nayttaminen UI:ssa

`HistoryView` (`/historia`): kayttaja valitsee kissan ComboBoxista, jolloin `CatHistoryService.findCatHistory(catId)` hakee Enversin `AuditReader`-rajapinnalla revisiot ja nayttaa ne Grid-listana (revision numero, toiminto INSERT/UPDATE/DELETE, aikaleima, kissan kentat).

Tiedostot:

- `src/main/java/com/example/demo/ui/view/HistoryView.java`
- `src/main/java/com/example/demo/service/CatHistoryService.java`

### F13 – Ulkoinen JavaScript-komponentti (Quill.js)

`RichTextNotesView` lataa Quill.js -kirjaston CDN:sta `@JavaScript` ja `@StyleSheet`-annotaatioilla ja alustaa editorin `executeJs()`-kutsulla. Kayttaja voi kirjoittaa muotoiltua tekstia (bold, italic, listat) ja tallentaa HTML-esikatstelun.

Tiedosto: `src/main/java/com/example/demo/ui/view/RichTextNotesView.java`

---

## Yhteenveto

| Osio                           | Toteutettu |
| ------------------------------ | ---------- |
| A) Data, entiteetit, CRUD      | 6/6        |
| B) Criteria API -suodattaminen | 5/5        |
| C) Tyylit ja ulkoasu           | 5/5        |
| D) SPA-rakenne                 | 5/5        |
| E) Autentikointi ja tietoturva | 6/6        |
| F) Muut toiminnallisuudet      | 13/13      |
| **Yhteensa**                   | **40/40**  |

Arvosana 5 (>= 30 kohtaa tayttyy).
