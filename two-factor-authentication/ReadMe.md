# Two-Factor Authentication with Spring Security

A small Spring Boot project demonstrating JPA-backed authentication with mandatory two-factor authentication using a password and One-Time Token (OTT).

## Stack

- Java 21
- Spring Boot 4.1
- Spring Security 7
- Spring Data JPA
- PostgreSQL
- Thymeleaf
- JavaMailSender
- Bootstrap

## Authentication Flow

```text
Username + Password
        ↓
JPA UserDetailsService
        ↓
FACTOR_PASSWORD
        ↓
Send verification code by email
        ↓
Enter One-Time Token
        ↓
FACTOR_OTT
        ↓
Role-based authorization
        ↓
ADMIN / MANAGER / USER page
```

Both authentication factors are required before access to protected resources is granted.

## Main Features

- Spring Security Multi-Factor Authentication
- Password + OTT factors
- Persistent OTT storage in PostgreSQL
- OTT expiration and single-use validation
- Email delivery of verification tokens
- HTML email templates with Thymeleaf
- Custom login and OTT verification pages
- Separate authentication failure handling for:
    - invalid username/password
    - invalid or expired OTT
- Role-based authorization
- Role-based redirect after successful MFA

## Roles

```text
ADMIN   → /admin
MANAGER → /manager
USER    → /user
```

Access is restricted using Spring Security authorization rules.

## Local Development

PostgreSQL and Mailpit are provided through Docker Compose.

Mailpit acts as a local SMTP server, so no real email account is required for testing verification codes.

For local development, `JavaMailSender` sends email to Mailpit instead of an external SMTP provider. The recipient addresses are intentionally non-existent demo addresses.

No SMTP username, password, authentication, or TLS configuration is required when using Mailpit locally.

```text
SMTP:       localhost:1025
Mailpit UI: http://localhost:8025
```

## Database

The same PostgreSQL database stores both application users and One-Time Tokens.

```text
PostgreSQL
├── application users       ← Spring Data JPA
└── one-time tokens         ← JdbcOneTimeTokenService
```

User authentication uses the custom JPA model, while OTT persistence is handled by Spring Security's `JdbcOneTimeTokenService`.

## Demo Users

The application can be tested with three predefined accounts:

```text
admin@example.test    / 123 → ADMIN
manager@example.test  / 123 → MANAGER
user@example.test     / 123 → USER
```

Passwords are stored in PostgreSQL using BCrypt.

Demo accounts are initialized from:

```text
src/main/resources/data.sql
```

The email addresses do not need to exist. Mailpit captures the verification emails locally.

## Run the Application

Start PostgreSQL and Mailpit:

```bash
docker compose up -d
```

Then run Spring Boot:

```bash
mvn spring-boot:run
```

The application runs at:

```text
http://localhost:8085
```

After requesting an OTT, open the Mailpit inbox:

```text
http://localhost:8025
```

The Mailpit inbox displays the generated verification email, including the HTML template and OTT.

## Scope

This project focuses on authentication and authorization.

User administration, account management, password reset, and user CRUD are intentionally outside the scope.

The project is intended as a small Spring Security pattern that can later be extended toward OAuth2/OIDC, an authorization server, API Gateway authentication, and token-based security for microservices.