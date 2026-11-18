# Core Java 21 Spring Boot 3 Spring Security 6 JWT

This project that contains the core with the technologies of java 21, spring boot 3, spring security 6, JWT, Conection to PostgreSQL

<p align="center">
  <img src="https://github.com/gianValentin/core-java21-springboot3/assets/45782176/4ad67cd9-4be7-4bc7-8cce-ca458bcf1b85" title="Java 21" alt="Java 21" width="100px" height="100px"/>
  <img src="https://github.com/gianValentin/core-java21-springboot3/assets/45782176/91f570e2-079b-40b7-a5fd-9af890d530b4" title="Spring Boot 3" alt="Spring Boot 3" width="110px" height="100px"/>  
  <img src="https://github.com/gianValentin/core-java21-springboot3/assets/45782176/34cf5d7b-e325-45ec-948f-d799b2244559" title="JWT" alt="Json Web Token" width="100px" height="100px"/>
  <img src="https://github.com/gianValentin/core-java21-springboot3/assets/45782176/91e795b5-cd19-4492-95bd-33f7465dfb31" title="PostgreSql" alt="PostgresSql" width="100px" height="100px"/>
</p>

## Feature

- Implements the new version of java 21 LTS, which is required to be used in spring boot 3
- Implement the new version spring boot 3 and spring framework 6
- Implement the new version spring security 6 using JWT
- Implement token refresh architecture in security

## Authentication configuration

Production must provide a newly generated Base64 JWT key through `JWT_SECRET_KEY` (at least 256 bits), and must rotate the key previously committed in older revisions. Never use the development key from the H2 profile in production.

Configure `PROD_ALLOWED_ORIGINS` with an explicit comma-separated list of frontend origins, such as `https://admin.example.com`. Production cookies require HTTPS (`AUTH_COOKIE_SECURE=true`). Public registration is disabled by default in the PostgreSQL profile; set `AUTH_REGISTRATION_ENABLED=true` only if customer self-registration is part of the deployment.

The authentication contracts are:

- `POST /api/v1/auth/login` accepts `{ "username", "password", "remember" }` and returns `{ "access_token", "username", "role" }`. The refresh token is issued only as an HttpOnly `refresh_token` cookie.
- `POST /api/v1/auth/refresh-token` accepts the refresh cookie, requires an allowed `Origin`, rotates the refresh token, and returns a new access token.
- `POST /api/v1/auth/logout` revokes the active token session and clears the refresh cookie; it also requires an allowed `Origin`.
- `POST /api/v1/auth/register` keeps the existing registration DTO and returns the same access-token response. New accounts are always assigned `USER`; the endpoint is disabled unless registration is explicitly enabled.

The access token expires after 15 minutes by default. Refresh tokens are rotated and stored hashed in the token table. The `remember` login option controls whether the HttpOnly refresh cookie is persistent or browser-session scoped.
