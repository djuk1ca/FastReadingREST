# FastReadingREST

REST API behind **PDF FastReading** — a speed-reading app that ingests a PDF, splits it into
word chunks, and streams them back to a client one word at a time (RSVP-style), while tracking
where each reader stopped and how fast they were going.

Built with **Java 21** and **Spring Boot 4**, backed by **PostgreSQL** (Neon), deployed as a
container on **Koyeb**.

| | |
|---|---|
| **Live demo (frontend)** | https://pdf-fast-reading.vercel.app/ |
| **API base URL** | `https://emotional-stesha-djuk1ca-24b70885.koyeb.app` |
| **Swagger UI** | [`/swagger-ui/index.html`](https://emotional-stesha-djuk1ca-24b70885.koyeb.app/swagger-ui/index.html) |
| **OpenAPI spec** | [`/v3/api-docs`](https://emotional-stesha-djuk1ca-24b70885.koyeb.app/v3/api-docs) |

> **Cold starts.** Both the API (Koyeb free tier) and the database (Neon free tier) scale to zero
> when idle. The first request after a quiet period can take **~30 seconds** while the container
> and the database wake up; subsequent requests return in well under a second. If the demo looks
> like it hung, it is booting — give it half a minute.

---

## Contents

- [Why the chunking design](#why-the-chunking-design)
- [Architecture](#architecture)
- [Tech stack](#tech-stack)
- [API reference](#api-reference)
- [Data model](#data-model)
- [Configuration](#configuration)
- [Running locally](#running-locally)
- [Deployment](#deployment)
- [Known limitations](#known-limitations)

---

## Why the chunking design

The interesting constraint in a speed reader is that the client consumes words at 300–1000 WPM,
which is 5–17 words *per second*. Fetching per word is out of the question, and shipping a whole
book to the browser up front is wasteful. So the API works in **word-indexed chunks**:

1. **On upload**, [`PdfChunkingService`](src/main/java/com/example/demo/services/PdfChunkingService.java)
   extracts text with PDFBox, collapses all whitespace to single spaces, tokenizes on whitespace,
   and slices the token stream into fixed **1200-word** chunks.
2. Each chunk is stored with its `chunk_index` **and its `start_word_index`** — the absolute
   position of its first word in the document. That second column is what makes everything else
   cheap.
3. **Reading position is a single integer**: the absolute word index. It is stored per
   `(user, document)` in `reading_progress`, so a reader resumes on any device from one number.
4. **On open**, [`ChunkReadService`](src/main/java/com/example/demo/services/ChunkReadService.java)
   resolves that integer to a chunk with one indexed query (`start_word_index <= wordIndex`,
   ordered descending, limit 1) and returns the chunk plus the reader's offset inside it.
5. **Prefetch**: once the offset within the current chunk passes `prefetchFrom` (default **800**
   of 1200), the response also carries the *next* chunk. The client therefore always has ~400
   words of runway before it needs the next one, and never blocks mid-stream.

The net effect is one round trip per ~1200 words rather than one per word, with resume-anywhere
for free.

## Architecture

```
                 ┌──────────────────────────────┐
   Vercel  ─────▶│  Controllers  (api layer)    │  JWT-authenticated, CORS-restricted
   frontend      └──────────────┬───────────────┘
                                │  DTO records (api/dto)
                 ┌──────────────▼───────────────┐
                 │  Services                    │  ingest, chunk-read, access, progress,
                 │                              │  sessions, reports
                 └──────────────┬───────────────┘
                                │  Spring Data JPA
                 ┌──────────────▼───────────────┐
                 │  Repositories  →  PostgreSQL │  Neon
                 └──────────────────────────────┘
```

Layout under `src/main/java/com/example/demo`:

| Package | Contents |
|---|---|
| `api/` | REST controllers, one per resource group |
| `api/dto/` | Request/response records — no entities cross the HTTP boundary |
| `api/error/` | Typed exceptions + `GlobalExceptionHandler` producing a uniform error body |
| `auth/` | Registration, login, JWT minting |
| `config/` | Security, CORS, JWT codec, password encoder, OpenAPI |
| `model/` | JPA entities |
| `repositories/` | Spring Data repositories, incl. native upsert and report aggregation |
| `security/` | `CurrentUser` — resolves user id / admin role from the JWT |
| `services/` | Business logic |

Every error response shares one shape:

```json
{
  "timestamp": "2026-09-07T13:32:39.849898168Z",
  "status": 401,
  "error": "UNAUTHORIZED",
  "message": "Invalid credentials",
  "path": "/auth/login"
}
```

## Tech stack

| Concern | Choice |
|---|---|
| Language / runtime | Java 21 |
| Framework | Spring Boot 4.0.3 (Web MVC, Data JPA, Validation, Actuator) |
| Security | Spring Security + OAuth2 Resource Server, JWT **HS256**, stateless |
| Database | PostgreSQL (Neon), HikariCP |
| PDF text extraction | Apache PDFBox 3.0.6 |
| PDF report generation | JasperReports 6.20.0 |
| API docs | springdoc-openapi 3.0.1 |
| Config | `spring-dotenv` — `.env` locally, real env vars in production |
| Build / deploy | Maven wrapper, multi-stage Docker, Koyeb |

## API reference

All routes require a `Bearer` JWT except `POST /auth/register`, `POST /auth/login`, and the
Swagger/OpenAPI routes. Note that **"public documents" means publicly *visible*, not
unauthenticated** — you still need a token to list or open them.

### Auth

| Method | Path | Body | Returns |
|---|---|---|---|
| `POST` | `/auth/register` | `{username, email, password}` | `201` `{id, username, email, role}` |
| `POST` | `/auth/login` | `{identifier, password}` | `200` `{accessToken, expiresInSeconds}` |

`identifier` accepts **either** the username or the email. Validation: username 3–50 chars,
password 6–100 chars, email must parse. New accounts always get role `USER`.

### Documents

| Method | Path | Notes |
|---|---|---|
| `GET` | `/public-documents` | Admin-published samples, `READY` only |
| `GET` | `/public-documents/{docId}/open?prefetchFrom=800` | Opens at the caller's saved position |
| `GET` | `/private-documents` | Documents owned by the caller |
| `POST` | `/private-documents` | `multipart/form-data`, part `file`, **query param** `title` |
| `GET` | `/private-documents/{docId}/open?prefetchFrom=800` | Owner only |

`open` returns the document metadata, the saved `currentWordIndex` and `lastWpm`, and a
`chunkAt` object holding the current chunk plus — past the prefetch threshold — the next one.

Upload limit is **50 MB**. Scanned/image-only PDFs are rejected: extraction yields no words, the
document is marked `FAILED`, and the request returns an error.

### Progress and sessions

| Method | Path | Body | Returns |
|---|---|---|---|
| `POST` | `/documents/{docId}/progress` | `{currentWordIndex, lastWpm}` | `200`, empty |
| `POST` | `/documents/{docId}/sessions/start` | `{mode}` — `"SPEED"` or `"NORMAL"` | `201` session |
| `POST` | `/documents/{docId}/sessions/{sessionId}/end` | `{avgWpm, wordsRead}` | `204` |

Progress writes go through a native `INSERT … ON CONFLICT DO UPDATE`, so they are a single
idempotent round trip — safe to call on a timer while reading.

### Admin — requires `ROLE_ADMIN`

| Method | Path | Notes |
|---|---|---|
| `GET` | `/admin/users` | All users |
| `POST` | `/admin/public-documents` | Publish a sample. `multipart/form-data`, parts `file` and `title` |
| `GET` | `/admin/reports/daily-words.pdf?date=YYYY-MM-DD` | JasperReports PDF: words read per user for that day |

> The `title` field is a **query parameter** on `/private-documents` but a **multipart part** on
> `/admin/public-documents`. That inconsistency is in the current code; clients must match it.

### Example

```bash
API=https://emotional-stesha-djuk1ca-24b70885.koyeb.app

TOKEN=$(curl -s -X POST "$API/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"identifier":"you@example.com","password":"your-password"}' \
  | sed -E 's/.*"accessToken":"([^"]+)".*/\1/')

curl -s "$API/public-documents" -H "Authorization: Bearer $TOKEN"
curl -s "$API/public-documents/1/open?prefetchFrom=800" -H "Authorization: Bearer $TOKEN"
```

## Data model

```
users ──┬─< documents (user_id)              owner of a PRIVATE document
        ├─< documents (uploaded_by_admin_id) admin who published a PUBLIC sample
        ├─< reading_sessions
        └─< reading_progress

documents ──┬─< document_chunks   unique (document_id, chunk_index)
            ├─< reading_sessions
            └─< reading_progress  PK (user_id, document_id)
```

| Column | Values |
|---|---|
| `documents.status` | `PROCESSING` → `READY` \| `FAILED` |
| `documents.visibility` | `PRIVATE` \| `PUBLIC` |
| `reading_sessions.mode` | `SPEED` \| `NORMAL` |
| `users.role` | `USER` \| `ADMIN` |

Only `READY` documents are readable — access checks in
[`DocumentAccessService`](src/main/java/com/example/demo/services/DocumentAccessService.java)
filter on it, so a half-ingested document is never served.

The app runs with `spring.jpa.hibernate.ddl-auto=validate` and **will not create its own tables**.
Apply [`db/schema.sql`](db/schema.sql) before first boot.

## Configuration

Every setting comes from the environment — there are no secrets in this repository. Locally,
`spring-dotenv` reads a git-ignored `.env`; on Koyeb the same keys are set as environment
variables. Copy [`.env.example`](.env.example) to get started.

| Variable | Required | Default | Notes |
|---|---|---|---|
| `DB_URL` | yes | — | JDBC URL, e.g. `jdbc:postgresql://host/db?sslmode=require` |
| `DB_USERNAME` | yes | — | |
| `DB_PASSWORD` | yes | — | |
| `DB_POOL_SIZE` | no | `2` | Small on purpose — Neon's free tier caps connections |
| `JWT_SECRET` | yes | — | HS256 key, **at least 32 bytes**; `openssl rand -base64 48` |
| `JWT_ISSUER` | no | `fast-reading` | |
| `JWT_EXP_SECONDS` | no | `26297438` | ~10 months — convenient for a demo, too long for production |
| `PORT` | no | `8080` | Injected by Koyeb |

CORS is allow-listed in
[`CorsSecurityConfig`](src/main/java/com/example/demo/config/CorsSecurityConfig.java) to
`http://localhost:5173` (Vite dev) and `https://pdf-fast-reading.vercel.app`. A new frontend
origin has to be added there.

## Running locally

**Prerequisites:** JDK 21 and a PostgreSQL database (a free Neon project works well).

```bash
git clone https://github.com/djuk1ca/FastReadingREST.git
cd FastReadingREST
cp .env.example .env
```

Fill in `DB_*` and `JWT_SECRET` in `.env`, create the schema, then run:

```bash
psql "postgresql://<user>:<pass>@<host>/<db>?sslmode=require" -f db/schema.sql
./mvnw spring-boot:run
```

On Windows use `mvnw.cmd spring-boot:run`. Then open http://localhost:8080/swagger-ui/index.html.

Registration always creates a `USER`. To get an admin, promote an existing account directly:

```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'you@example.com';
```

Log in again afterwards — the role is baked into the JWT at login, so an existing token keeps the
old role until it expires.

### With Docker

```bash
docker build -t fastreading-api .
docker run --rm -p 8080:8080 --env-file .env fastreading-api
```

### Tests

```bash
./mvnw test
```

`ApplicationTests` is a `@SpringBootTest` that boots the full context, so it needs a reachable
database and a valid `JWT_SECRET`. Without them it fails on context startup — which is why the
Docker build runs `-DskipTests`.

## Deployment

Deployed on **Koyeb's free tier** from this repository's `Dockerfile`:

- **Build** — multi-stage: Maven + Temurin 21 to build the jar, `eclipse-temurin:21-jre` to run
  it. Only the jar reaches the final image.
- **Port** — Koyeb injects `PORT`; `application.properties` reads it as `${PORT:8080}`.
- **Secrets** — `DB_*` and `JWT_SECRET` are configured as Koyeb environment variables.
- **Scale to zero** — the free instance sleeps when idle, hence the cold start described at the
  top. Neon's free tier suspends its compute the same way, which is also why `DB_POOL_SIZE`
  defaults to 2.

The frontend is a separate project deployed on Vercel at
[pdf-fast-reading.vercel.app](https://pdf-fast-reading.vercel.app/).

## Known limitations

Honest notes on where this sits — it is a portfolio/coursework project, not a hardened service.

- **No refresh tokens.** A single long-lived (~10 month) access token. Fine for a demo; a real
  deployment wants short access tokens plus rotation, and a way to revoke.
- **Actuator is not reachable.** `management.endpoints.web.exposure.include=health` exposes
  `/actuator/health`, but `SecurityConfig` ends in `anyRequest().authenticated()` without
  permitting it, so it answers `401`. Health checks need that path allow-listed first.
- **Ingest is synchronous.** A large PDF is parsed and chunked inside the upload request. Past a
  certain size this should move to a background job with the document left in `PROCESSING`.
- **Chunks are stored as rows of text.** Simple and queryable, but a large document meaningfully
  inflates the database. Object storage with an index table would scale better.
- **No pagination** on `/admin/users` or the document lists.
- **Scanned PDFs are unsupported** — there is no OCR fallback, so image-only PDFs fail on upload.
- **Test coverage is a smoke test only** (`contextLoads`).

## License

Released under the [MIT License](LICENSE) - free to use, modify, and distribute with attribution.
