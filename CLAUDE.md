# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**CatShelter** — a cat shelter management system built with Spring Boot 3.4.4, Vaadin Flow 24.6.6, and Java 21. All UI is server-side Java (Vaadin Flow); there is no separate frontend framework or REST API.

## Commands

```bash
mvn spring-boot:run          # Run locally (H2 in-memory DB, opens browser automatically)
mvn clean install            # Build executable JAR
mvn test                     # Run tests
docker compose up --build    # Run with PostgreSQL (production profile)
```

The application runs on `http://localhost:8080`. H2 console is available at `/h2-console` in dev mode.

## Architecture

**Layering:**

1. **UI** (`ui/view/`) — Vaadin Flow views. Each view is a `@Route`-annotated Java class. `MainLayout.java` provides the shell (sidebar nav, header).
2. **Security** (`SecurityConfig.java`) — Extends `VaadinWebSecurity`. Form login + optional OAuth2 (GitHub if env vars are set). Role-based access via `@RolesAllowed` annotations on views.
3. **Service** (`service/`) — Business logic: password reset with email, registration notifications, audit history (Envers), profile image storage, and advanced cat search via JPA Criteria API (`CatSearchService`).
4. **Repository** (`repository/`) — Spring Data JPA repositories. `CatSearchService` uses Criteria API for dynamic multi-predicate queries.
5. **Entity** (`entity/`) — JPA entities, all extending `AuditableEntity` (adds `createdAt`/`updatedAt`). All entities are `@Audited` (Hibernate Envers tracks full revision history).

## Domain Model

- **Cat** — core entity. Has a one-to-one `HealthRecord`, one-to-many `AdoptionApplication`s, and many-to-many `Tag`s.
- **AppUser** — user accounts with a `Set<Role>` (`ROLE_ADMIN`, `ROLE_SUPER`, `ROLE_USER`).
- **AdoptionApplication** — tracks adoption requests with `AdoptionStatus` enum (SUBMITTED → IN_REVIEW → APPROVED/REJECTED).
- **Tag** — categorization tags with color/icon metadata for UI display.
- **PasswordResetToken** — short-lived tokens for email-based password reset.

All entity fields use Bean Validation annotations (`@NotBlank`, `@Size`, `@DecimalMin`, etc.).

## Database Profiles

| Profile       | DB            | DDL           | Trigger               |
| ------------- | ------------- | ------------- | --------------------- |
| default (dev) | H2 in-memory  | `create-drop` | `mvn spring-boot:run` |
| docker        | PostgreSQL 16 | `update`      | `docker compose up`   |

Switch profiles via `spring.profiles.active=docker` or `SPRING_PROFILES_ACTIVE=docker`.

## Default Users (created by `DataInitializer` on startup)

| Username  | Password | Role       |
| --------- | -------- | ---------- |
| admin     | admin123 | ROLE_ADMIN |
| superuser | super123 | ROLE_SUPER |
| user      | user123  | ROLE_USER  |

## Route Map

| View class               | Route               | Min role      |
| ------------------------ | ------------------- | ------------- |
| DashboardView            | `/`                 | anonymous     |
| LoginView                | `/login`            | anonymous     |
| RegisterView             | `/register`         | anonymous     |
| CatsView                 | `/kissat`           | authenticated |
| CatSearchView            | `/haku`             | authenticated |
| AdoptionApplicationsView | `/adoptioanomukset` | authenticated |
| RichTextNotesView        | `/muistiinpanot`    | authenticated |
| HistoryView              | `/historia`         | authenticated |
| HealthRecordsView        | `/terveyskortit`    | ROLE_SUPER    |
| TagsView                 | `/tagit`            | ROLE_ADMIN    |

## Key Patterns

- **Vaadin Grid + Dialog CRUD**: Views use a `Grid<T>` for listing and a `Dialog` with a `FormLayout` for add/edit. See `CatsView` for the canonical example.
- **Audit trail**: Use `CatHistoryService` (wraps Hibernate Envers `AuditReader`) to retrieve revision history. `HistoryView` renders it.
- **Advanced search**: `CatSearchService.search(CatSearchFilter)` builds Criteria predicates dynamically — add new filter fields there. Always uses `LEFT JOIN` to include cats without tags.
- **Email**: Services inject `Optional<JavaMailSender>` and log a fallback message when mail is not configured — don't remove that pattern.
- **Profile images**: Stored on filesystem via `ProfileImageStorageService`. Max 2 MB, whitelist of image extensions.
- **UI language**: All labels, placeholders, and messages are in Finnish.
