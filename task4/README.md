# Spring Security Demo

Spring Boot MVC application demonstrating Spring Security: 
- form login with email/password, 
- salted password hashing, 
- permission-based authorization, 
- custom login/logout pages and brute-force protection. 
Users are stored in a Neon (serverless PostgreSQL) database.

## Stack

- Java 21+, Maven, Spring Boot 3.5
- Spring MVC, Spring Security, Spring Data JPA, Thymeleaf
- PostgreSQL (Neon)
- Python 3 + `requests` for end-to-end tests

## Configuration

The database is configured via environment variables (no secrets in the repo):

| Variable      | Example                                                                                   |
|---------------|-------------------------------------------------------------------------------------------|
| `DB_URL`      | `jdbc:postgresql://<neon-host>/neondb?sslmode=require`                                    |
| `DB_USER`     | `neondb_owner`                                                                            |
| `DB_PASSWORD` | your Neon password                                                                        |

Convert a Neon `postgresql://user:pass@host/db?sslmode=require` URL 
to JDBC form: `jdbc:postgresql://host/db?sslmode=require`, 
with user and password supplied separately.

Brute-force settings in `application.properties`:
```
app.security.max-failed-attempts=3
app.security.block-duration-minutes=5
```

## Run

PowerShell:

```powershell
$env:DB_URL="jdbc:postgresql://<neon-host>/neondb?sslmode=require"
$env:DB_USER="neondb_owner"
$env:DB_PASSWORD="<password>"
mvn spring-boot:run
```

The app starts on <http://localhost:8080>. 
Tables are created automatically (`ddl-auto=update`) and demo users are seeded on first start. 
Use `--server.port=8081` if 8080 is taken.

## Endpoints

| Endpoint            | Access                      | Description                          |
|---------------------|-----------------------------|--------------------------------------|
| `GET /about`        | public                      | App name and version                 |
| `GET /login`        | public                      | Login page (POST `/login`)           |
| `POST /logout`      | authenticated (CSRF token)  | Logout, redirects to `/login?logout` |
| `GET /`             | authenticated               | Home page with permissions and links |
| `GET /info`         | `VIEW_INFO`                 | Random stats                         |
| `GET /admin`        | `VIEW_ADMIN`                | Admin message                        |
| `GET /blocked-users`| `VIEW_ADMIN`                | Currently blocked emails + time left |

Unauthenticated requests are redirected to `/login`; authenticated users without the required permission get `403`.

## Demo users

| Email                   | Password        | Permissions                |
|-------------------------|-----------------|----------------------------|
| `admin@example.com`     | `Admin#123`     | `VIEW_INFO`, `VIEW_ADMIN`  |
| `info@example.com`      | `Info#123`      | `VIEW_INFO`                |
| `adminonly@example.com` | `AdminOnly#123` | `VIEW_ADMIN`               |
| `locktest@example.com`  | `LockTest#123`  | `VIEW_INFO` (used by the brute-force test) |

## Security details

- **Passwords:** hashed with BCrypt (cost 10). Each hash contains a random per-password salt; only the hash is stored.
- **Authorization:** permissions are stored in `app_user_permissions` and mapped to Spring authorities, 
- enforced in `SecurityConfig`.
- **Brute-force protection:** `LoginAttemptService` counts consecutive bad-credential failures per email. 
- After 3 failures the email is blocked for 5 minutes; even the correct password is rejected (`/login?blocked`) 
- until the block expires. A successful login resets the counter. State is in memory, so a restart clears blocks.

## End-to-end test

With the app running:

```powershell
pip install requests
python src/test/python/e2e_test.py [base_url]
```

It checks public/anonymous access, the permission matrix for each user, bad credentials, 
logout and the brute-force block (including `/blocked-users`). 
The brute-force part blocks `locktest@example.com` for 5 minutes and is skipped 
if that user is already blocked; restart the app to clear it.
