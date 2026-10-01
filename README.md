# Portfolio backend

Spring Boot 3.5 (Java 17) API behind the portfolio site
(https://tiagorodrigues-gith.github.io/tiago-rodrigues-portfolio/):

| Endpoint | Access | Purpose |
|---|---|---|
| `GET /api/health`, `GET /api/projects`, `GET /api/projects/{id}` | public | health check, published projects |
| `POST /api/auth/login` | public, rate limited | e-mail + password → JWT (HS256, scope `ADMIN`, 2 h) |
| `POST /api/analytics/visit` | public, rate limited | records one page view (IP from the connection) |
| `GET /api/admin/analytics/summary?days=30`, `GET /api/admin/analytics/visits?limit=100` | admin | visit statistics for the admin page |
| `POST/PUT/DELETE /api/projects/**`, `GET /api/projects/all` | admin | project management |

## Security model

- **One admin account**, created at start-up from `ADMIN_EMAIL` and `ADMIN_PASSWORD_HASH`
  (a BCrypt hash; the plain password is never stored or configured).
- **JWT** signed with `JWT_SECRET` (≥ 32 bytes, Base64 or text). Stateless: no session,
  no cookie, so no CSRF surface. The site keeps the token in `sessionStorage` and drops it
  when it expires.
- **Brute force**: 5 failed logins from one address lock it for 15 minutes (HTTP 429).
  Unknown e-mails and wrong passwords take the same time and get the same answer.
- **CORS** only for `CORS_ALLOWED_ORIGINS`; everything not listed above is denied.
- **Headers**: `Content-Security-Policy: default-src 'none'`, `X-Frame-Options: DENY`,
  `X-Content-Type-Options: nosniff`, `Referrer-Policy: no-referrer`, HSTS.
- **Errors** never return internal messages or stack traces.

## Visit statistics and LGPD

Stored per page view: IP address, user agent, page, language, first-page referrer, screen size,
time zone, a random per-tab visit id. Browsers that send `Sec-GPC: 1` or `DNT: 1` are not
recorded (the site also checks this before sending). Records are deleted after
`ANALYTICS_RETENTION_DAYS` (default 90) by a daily job. The site's privacy page
(`/privacy`) describes this to visitors; review its wording with someone who knows LGPD/GDPR
before relying on it.

## Configuration (environment variables)

| Variable | Required | Example / default |
|---|---|---|
| `ADMIN_EMAIL` | yes (for login) | your address |
| `ADMIN_PASSWORD_HASH` | yes (for login) | output of `htpasswd -bnBC 12 "" 'your-password' \| tr -d ':\n'` |
| `JWT_SECRET` | yes in production | `openssl rand -base64 32` |
| `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD` | recommended | `jdbc:postgresql://host:5432/db`; default is in-memory H2 (data lost on restart) |
| `CORS_ALLOWED_ORIGINS` | no | `https://tiagorodrigues-gith.github.io,http://localhost:4200` |
| `ANALYTICS_RETENTION_DAYS` | no | `90` |
| `PORT` | set by the host | `8080` |

## Run and test

```bash
mvn test                      # integration tests: login, rate limit, admin access, visits, CORS, headers
mvn spring-boot:run           # http://localhost:8080 (H2 in memory)
```

The frontend's `npm run start:proxy` forwards `/api` to this server on port 8080.

## Deploy (example: Render + a free PostgreSQL)

1. Create a PostgreSQL database (Render, Neon or Supabase) and note its JDBC URL, user and password.
2. On Render: *New → Web Service → this repository*; it builds the `Dockerfile`.
3. Set the environment variables above (never commit them).
4. Check `https://<service>.onrender.com/api/health` returns `OK`.
5. In the frontend, set `apiUrl` in `src/environments/environment.prod.ts` to the service origin and add
   the same origin to `connect-src` in the CSP meta tag of `src/index.html`; push to `main`.

Free instances sleep when idle, so the first request after a pause can take a while; visits sent during
that time are simply lost, which is acceptable for statistics.
