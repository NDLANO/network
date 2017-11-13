/*
 * Part of NDLA network.
 * Copyright (C) 2017 NDLA
 *
 * See LICENSE
 */

package no.ndla.network.jwt

import javax.servlet.http.HttpServletRequest

import no.ndla.network.UnitSuite
import org.mockito.Mockito.when

class JWTExtractorTest extends UnitSuite {

  val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL25kbGEuZXUuYXV0aDAuY29tLyIsInN1YiI6Imdvb2dsZS1vYXV0aDJ8MDAwMDAwMDAwMCIsImF1ZCI6Im5kbGFfc3lzdGVtIiwiYXpwIjoiV1UwS3I0Q0Rrck0wdUwiLCJleHAiOjE1MDYzNTI4NjEsImlhdCI6MTUwNjM0NjkwMywic2NvcGUiOiJsaXN0aW5nLXRlc3Q6d3JpdGUgYXJ0aWNsZXMtdGVzdDp3cml0ZSBhcnRpY2xlcy1zdGFnaW5nOndyaXRlIGF1ZGlvLXN0YWdpbmc6d3JpdGUiLCJodHRwczovL25kbGEubm8vbmRsYV9pZCI6ImRldHRlX2VyX2VuX25kbGFfaWQiLCJodHRwczovL25kbGEubm8vdXNlcl9uYW1lIjoiVGVzdCBUZXN0ZXNlbiIsImp0aSI6Ijg5MzAwNjhhLTMxOTMtNGNiOC04YjU2LWU3NTcyN2FiN2ZkNSJ9.UXmiSZL0ftV1fix7aVDMeFa1T23nB2ufT-6qdInrtes"

  def setEnv(key: String, value: String) = {
    val field = System.getenv().getClass.getDeclaredField("m")
    field.setAccessible(true)
    val map = field.get(System.getenv()).asInstanceOf[java.util.Map[java.lang.String, java.lang.String]]
    map.put(key, value)
  }

  test("That userId is None when no authorization header is set") {
    new JWTExtractor(mock[HttpServletRequest]).extractUserId() should be(None)
  }

  test("That userId is None when an illegal JWT is set") {
    val request = mock[HttpServletRequest]
    when(request.getHeader("Authorization")).thenReturn("This is an invalid JWT")

    new JWTExtractor(request).extractUserId() should be(None)
  }

  test("That userId is None when no ndla_id is present") {
    val tokenWithoutUserId = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL25kbGEuZXUuYXV0aDAuY29tLyIsInN1YiI6Imdvb2dsZS1vYXV0aDJ8MDAwMDAwMDAwMCIsImF1ZCI6Im5kbGFfc3lzdGVtIiwiYXpwIjoiV1UwS3I0Q0Rrck0wdUwiLCJleHAiOjE1MDY1MTg3NjQsImlhdCI6MTUwNjM0NjkwMywic2NvcGUiOiJsaXN0aW5nLXRlc3Q6d3JpdGUgYXJ0aWNsZXMtdGVzdDp3cml0ZSBhcnRpY2xlcy1zdGFnaW5nOndyaXRlIGF1ZGlvLXN0YWdpbmc6d3JpdGUiLCJodHRwczovL25kbGEubm8vdXNlcl9uYW1lIjoiVGVzdCBUZXN0ZXNlbiIsImp0aSI6IjNmYmNlNDk1LTlmMDMtNDE0Ny1hNjcyLTVmZjYzOGVjMDkyNCJ9.aWQ2lqSXzsMgr_dd5S5xHKKGqPVRX7LdnH2nkSUlM-0"
    val request = mock[HttpServletRequest]
    when(request.getHeader("Authorization")).thenReturn(s"Bearer $tokenWithoutUserId")
    new JWTExtractor(request).extractUserId() should be(None)
  }

  test("That JWTExtractor.extractUserId is set even if roles are not present") {
    val tokenWithoutRoles = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL25kbGEuZXUuYXV0aDAuY29tLyIsInN1YiI6Imdvb2dsZS1vYXV0aDJ8MDAwMDAwMDAwMCIsImF1ZCI6Im5kbGFfc3lzdGVtIiwiYXpwIjoiV1UwS3I0Q0Rrck0wdUwiLCJleHAiOjE1MDY1MTc1ODQsImlhdCI6MTUwNjM0NjkwMywiaHR0cHM6Ly9uZGxhLm5vL25kbGFfaWQiOiJkZXR0ZV9lcl9lbl9uZGxhX2lkIiwiaHR0cHM6Ly9uZGxhLm5vL3VzZXJfbmFtZSI6IlRlc3QgVGVzdGVzZW4iLCJqdGkiOiJiNGVlZmQwZi0zNjg1LTQwMWItYjY3MC02MzUyY2NmNGQzNTgifQ.8vEYDokCZYAIz1Vq7R-NfU0NrcI9hpoIL7316fFYF_A"
    val request = mock[HttpServletRequest]
    when(request.getHeader("Authorization")).thenReturn(s"Bearer $tokenWithoutRoles")
    val jWTExtractor = new JWTExtractor(request)
    jWTExtractor.extractUserId() should equal(Some("dette_er_en_ndla_id"))
    jWTExtractor.extractUserRoles() should equal(List.empty)
  }

  test("That all roles for correct environment are extracted") {
    setEnv("NDLA_ENVIRONMENT", "test")

    val request = mock[HttpServletRequest]
    when(request.getHeader("Authorization")).thenReturn(s"Bearer $token")

    val jwtExtractor = new JWTExtractor(request)
    val roles = jwtExtractor.extractUserRoles()
    roles.size should be(2)
    roles.contains("listing:write") should be(true)
    roles.contains("articles:write") should be(true)
  }

  test("That legacy role definitions are extracted") {
    setEnv("NDLA_ENVIRONMENT", "staging")
    val tokenWithLegacyRoles = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL25kbGEuZXUuYXV0aDAuY29tLyIsInN1YiI6Imdvb2dsZS1vYXV0aDJ8MDAwMDAwMDAwMCIsImF1ZCI6Im5kbGFfc3lzdGVtIiwiYXpwIjoiV1UwS3I0Q0Rrck0wdUwiLCJleHAiOjE1MDY1MjQwNDUsImlhdCI6MTUwNjM0NjkwMywic2NvcGUiOiJsaXN0aW5nOndyaXRlIGFydGljbGVzOndyaXRlIGF1ZGlvLXN0YWdpbmc6d3JpdGUiLCJodHRwczovL25kbGEubm8vbmRsYV9pZCI6ImRldHRlX2VyX2VuX25kbGFfaWQiLCJodHRwczovL25kbGEubm8vdXNlcl9uYW1lIjoiVGVzdCBUZXN0ZXNlbiIsImp0aSI6IjhmNTJjZjk5LWUwMzEtNDc3Yy04ZDg3LTk4ODczZjRlOWY0NiJ9.NMUgVTmaGjGwCP3uh-wXFGUHC0Eo9hM4CJQg9de41mM"
    val request = mock[HttpServletRequest]
    when(request.getHeader("Authorization")).thenReturn(s"Bearer $tokenWithLegacyRoles")

    val jwtExtractor = new JWTExtractor(request)
    val roles = jwtExtractor.extractUserRoles()
    roles.size should be(3)
    roles.contains("listing:write") should be(true)
    roles.contains("articles:write") should be(true)
    roles.contains("audio:write") should be(true)
  }

  test("That name is extracted") {
    val request = mock[HttpServletRequest]
    when(request.getHeader("Authorization")).thenReturn(s"Bearer $token")

    val jwtExtractor = new JWTExtractor(request)
    val name = jwtExtractor.extractUserName() should equal(Some("Test Testesen"))
  }

  // Legacyformat tests
  test("That userId is None when no app-metadata is present in legacy format") {
    val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL3NvbWUtZG9tYWluLyIsInN1YiI6Imdvb2dsZS1vYXV0aDJ8MTIzIiwiYXVkIjoiYXNkZmFzZGYiLCJleHAiOjE0ODYwNzAwNjMsImlhdCI6MTQ4NjAzNDA2M30.KEjhvPUooLSFExTrv8XsioJks-NAMzYZjGn32MABvg4"
    val request = mock[HttpServletRequest]
    when(request.getHeader("Authorization")).thenReturn(s"Bearer $token")
    new JWTExtractor(request).extractUserId() should be(None)
  }

  test("That JWTExtractor.extractUserId is set even if roles are not present in legacy format") {
    val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhcHBfbWV0YWRhdGEiOnsibmRsYV9pZCI6ImFiYzEyMyJ9LCJpc3MiOiJodHRwczovL3NvbWUtZG9tYWluLyIsInN1YiI6Imdvb2dsZS1vYXV0aDJ8MTIzIiwiYXVkIjoiYXNkZmEiLCJleHAiOjE0ODYwNzAwNjMsImlhdCI6MTQ4NjAzNDA2M30.HT3SUUZe52dC4y1FPJb3gpKA5n56WxVM4CmEZDgmpko"
    val request = mock[HttpServletRequest]
    when(request.getHeader("Authorization")).thenReturn(s"Bearer $token")
    val jWTExtractor = new JWTExtractor(request)
    jWTExtractor.extractUserId() should equal(Some("abc123"))
    jWTExtractor.extractUserRoles() should equal(List.empty)
    jWTExtractor.extractClientId() should be(None)
  }

  test("That JWTExtractor.extractClientId is set if ndla_id is legacy clientId") {
    val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhcHBfbWV0YWRhdGEiOnsibmRsYV9pZCI6ImZkamFza2ZsamRsc2FmZC5lZGl0b3JpYWwtZnJvbnRlbmQifSwiaXNzIjoiaHR0cHM6Ly9zb21lLWRvbWFpbi8iLCJzdWIiOiJnb29nbGUtb2F1dGgyfDEyMyIsImF1ZCI6ImFzZGZhIiwiZXhwIjoxNDg2MDcwMDYzLCJpYXQiOjE0ODYwMzQwNjN9.QQNvAYveIjpMX58Bp5aYA7xByy1xoIbW3V9Tmv0PBik"
    val request = mock[HttpServletRequest]
    when(request.getHeader("Authorization")).thenReturn(s"Bearer $token")
    val jWTExtractor = new JWTExtractor(request)
    jWTExtractor.extractClientId() should equal(Some("fdjaskfljdlsafd.editorial-frontend"))
    jWTExtractor.extractUserRoles() should equal(List.empty)
    jWTExtractor.extractUserId() should be(None)
  }

  test("That all roles are extracted in legacy format") {
    val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhcHBfbWV0YWRhdGEiOnsicm9sZXMiOlsicm9sZTEiLCJyb2xlMiIsInJvbGUzIl0sIm5kbGFfaWQiOiJhYmMxMjMifSwibmFtZSI6IkRvbmFsZCBEdWNrIiwiaXNzIjoiaHR0cHM6Ly9zb21lLWRvbWFpbi8iLCJzdWIiOiJnb29nbGUtb2F1dGgyfDEyMyIsImF1ZCI6ImFiYyIsImV4cCI6MTQ4NjA3MDA2MywiaWF0IjoxNDg2MDM0MDYzfQ.qu8ecEgZiFN8QFL3Jq6SFNL3FMxVvTZTYv7SZ2moyJw"
    val request = mock[HttpServletRequest]
    when(request.getHeader("Authorization")).thenReturn(s"Bearer $token")

    val jwtExtractor = new JWTExtractor(request)
    val roles = jwtExtractor.extractUserRoles()
    roles.size should be(3)
    roles.contains("role1") should be(true)
    roles.contains("role2") should be(true)
    roles.contains("role3") should be(true)
  }

  test("That name is extracted in legacy format") {
    val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhcHBfbWV0YWRhdGEiOnsicm9sZXMiOlsicm9sZTEiLCJyb2xlMiIsInJvbGUzIl0sIm5kbGFfaWQiOiJhYmMxMjMifSwibmFtZSI6IkRvbmFsZCBEdWNrIiwiaXNzIjoiaHR0cHM6Ly9zb21lLWRvbWFpbi8iLCJzdWIiOiJnb29nbGUtb2F1dGgyfDEyMyIsImF1ZCI6ImFiYyIsImV4cCI6MTQ4NjA3MDA2MywiaWF0IjoxNDg2MDM0MDYzfQ.qu8ecEgZiFN8QFL3Jq6SFNL3FMxVvTZTYv7SZ2moyJw"
    val request = mock[HttpServletRequest]
    when(request.getHeader("Authorization")).thenReturn(s"Bearer $token")

    val jwtExtractor = new JWTExtractor(request)
    val name = jwtExtractor.extractUserName() should equal(Some("Donald Duck"))
  }
}
