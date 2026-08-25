package com.skycoin4444.oauth

import java.time.{Clock, Instant, ZoneOffset}
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class OAuthLabTest:
  private val redirect = "https://app.example/callback"
  private val verifier = "v" * 43

  @Test def authorizationCodeExchangesOnceWithPkce(): Unit =
    val lab = OAuthLab(Map("client-1" -> Set(redirect)))
    val code = lab.authorize("client-1", redirect, "user-1", OAuthLab.s256(verifier))
    val token = lab.exchange(code.value, "client-1", redirect, verifier)
    assertEquals("user-1", token.subject)
    assertEquals("client-1", token.clientId)
    assertEquals(0, lab.pendingCodeCount)
    assertThrows(classOf[IllegalArgumentException], () => lab.exchange(code.value, "client-1", redirect, verifier))

  @Test def rejectsWrongRedirectAndPkce(): Unit =
    val lab = OAuthLab(Map("client-1" -> Set(redirect)))
    assertThrows(classOf[IllegalArgumentException], () => lab.authorize("client-1", "https://evil.example/cb", "user-1", OAuthLab.s256(verifier)))
    val code = lab.authorize("client-1", redirect, "user-1", OAuthLab.s256(verifier))
    val wrong = "x" * 43
    assertThrows(classOf[IllegalArgumentException], () => lab.exchange(code.value, "client-1", redirect, wrong))

  @Test def expiredCodeIsRejected(): Unit =
    val fixed = Instant.parse("2026-08-24T00:00:00Z")
    val issueClock = Clock.fixed(fixed, ZoneOffset.UTC)
    val lab = OAuthLab(Map("client-1" -> Set(redirect)), issueClock)
    val code = lab.authorize("client-1", redirect, "user-1", OAuthLab.s256(verifier))
    assertTrue(code.expiresAt.isAfter(fixed))
