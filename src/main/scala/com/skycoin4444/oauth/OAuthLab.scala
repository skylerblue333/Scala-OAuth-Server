package com.skycoin4444.oauth

import java.nio.charset.StandardCharsets
import java.security.{MessageDigest, SecureRandom}
import java.time.{Clock, Duration, Instant}
import java.util.Base64
import scala.collection.concurrent.TrieMap

final class OAuthLab(
    clients: Map[String, Set[String]],
    clock: Clock = Clock.systemUTC(),
    random: SecureRandom = new SecureRandom(),
    codeTtl: Duration = Duration.ofMinutes(5),
    tokenTtl: Duration = Duration.ofHours(1)
):
  require(clients.nonEmpty, "at least one client is required")

  final case class AuthorizationCode(value: String, expiresAt: Instant)
  final case class AccessToken(value: String, expiresAt: Instant, subject: String, clientId: String)
  private final case class Pending(clientId: String, redirectUri: String, subject: String, challenge: String, expiresAt: Instant)

  private val codes = TrieMap.empty[String, Pending]

  def authorize(clientId: String, redirectUri: String, subject: String, codeChallenge: String): AuthorizationCode =
    validateClient(clientId, redirectUri)
    require(subject.matches("[A-Za-z0-9_.@-]{1,128}"), "invalid subject")
    require(codeChallenge.matches("[A-Za-z0-9_-]{43,128}"), "invalid S256 code challenge")
    val code = randomToken(32)
    val expires = clock.instant().plus(codeTtl)
    codes.put(code, Pending(clientId, redirectUri, subject, codeChallenge, expires))
    AuthorizationCode(code, expires)

  def exchange(code: String, clientId: String, redirectUri: String, verifier: String): AccessToken =
    require(code != null && code.matches("[A-Za-z0-9_-]{20,128}"), "invalid authorization code")
    require(verifier != null && verifier.matches("[A-Za-z0-9._~-]{43,128}"), "invalid PKCE verifier")
    val pending = codes.remove(code).getOrElse(throw IllegalArgumentException("authorization code invalid or already used"))
    require(clock.instant().isBefore(pending.expiresAt), "authorization code expired")
    require(pending.clientId == clientId && pending.redirectUri == redirectUri, "client or redirect mismatch")
    require(constantTimeEquals(pending.challenge, s256(verifier)), "PKCE verification failed")
    val expires = clock.instant().plus(tokenTtl)
    AccessToken(randomToken(32), expires, pending.subject, clientId)

  def pendingCodeCount: Int = codes.size

  private def validateClient(clientId: String, redirectUri: String): Unit =
    val redirects = clients.getOrElse(clientId, throw IllegalArgumentException("unknown client"))
    require(redirects.contains(redirectUri), "redirect URI not registered")

  private def randomToken(bytes: Int): String =
    val value = Array.ofDim[Byte](bytes)
    random.nextBytes(value)
    Base64.getUrlEncoder.withoutPadding().encodeToString(value)

  private def constantTimeEquals(left: String, right: String): Boolean =
    MessageDigest.isEqual(left.getBytes(StandardCharsets.US_ASCII), right.getBytes(StandardCharsets.US_ASCII))

  private def s256(verifier: String): String = OAuthLab.s256(verifier)

object OAuthLab:
  def s256(verifier: String): String =
    require(verifier != null, "verifier required")
    val digest = MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII))
    Base64.getUrlEncoder.withoutPadding().encodeToString(digest)
