# CatShelter

Kissatarhan hallintajärjestelmä. Vaadin 24 + Spring Boot 3.4 + Java 21.

GitHub OAuth client-id ja client-secret on commitoitu repoon tarkoituksellisesti opettajan testaamista varten. Callback URL on rajoitettu localhost:8080-osoitteeseen.

## Käynnistys

```bash
mvn spring-boot:run
```

Avaa: http://localhost:8080

### Docker (PostgreSQL)

```bash
docker compose up --build
```

## Työraportti

TYORAPORTTI.md

## Tunnukset

| Käyttäjä  | Salasana | Rooli      |
| --------- | -------- | ---------- |
| admin     | admin123 | ROLE_ADMIN |
| superuser | super123 | ROLE_SUPER |
| user      | user123  | ROLE_USER  |

## Ominaisuudet

- Kissojen, terveyskortien, adoptioanomuksien ja tagien hallinta (CRUD)
- Edistynyt haku Criteria API:lla
- Roolipohjainen käyttöoikeuksien hallinta (Spring Security)
- Rekisteröityminen ja profiilikuvan lataus
- Salasanan palautus sähköpostitse
- GitHub OAuth2 -kirjautuminen
- CSV-tuonti ja -vienti
- Muutoshistoria (Hibernate Envers)
- Quill.js rich text -editori
- Docker-tuki (app + PostgreSQL)
