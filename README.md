# Sky OAuth Authorization-Code Lab (Scala)

**Status: engineering beta / security learning product.** This repository now contains a real Scala 3 authorization-code + PKCE core instead of the previous Python placeholder.

## Implemented

- registered client and exact redirect-URI checks
- single-use authorization codes
- PKCE S256 challenge verification
- constant-time challenge comparison
- short authorization-code TTL and access-token TTL
- cryptographically random opaque codes/tokens
- bounded client, subject, code, and verifier inputs
- expiry and replay rejection
- Scala/JUnit verification through Maven
- dependency audit and non-root container smoke gate

## Build and test

```bash
mvn clean verify
```

## Container smoke run

```bash
docker build -t sky-scala-oauth .
docker run --rm sky-scala-oauth
```

## Security and product boundary

This is **not a complete OAuth 2.0/OIDC authorization server** and must not be presented as one. It does not implement browser authorization/consent UI, client authentication, refresh tokens, token introspection/revocation, OIDC ID tokens/discovery/JWKS, scopes/claims, user authentication, durable persistence, key rotation, issuer metadata, rate limiting, audit-log durability, HA, or production deployment.

Its commercial/engineering value is as a small, reviewable PKCE authorization-code primitive and reference implementation. A production identity service should use a mature audited OAuth/OIDC provider or undergo a substantially broader standards/security program.

## SKYCOIN4444 role

Potential identity/security lab component only. The canonical ecosystem identity boundary should consume stable standards-based interfaces instead of copying this implementation directly into a flagship application.

## License

See `LICENSE`.
