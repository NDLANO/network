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

  test("That userId is None when no authorization header is set"){
    new JWTExtractor(mock[HttpServletRequest]).extractUserId() should be (None)
  }

  test("That userId is None when an illegal JWT is set") {
    val request = mock[HttpServletRequest]
    when(request.getHeader("Authorization")).thenReturn("This is an invalid JWT")

    new JWTExtractor(request).extractUserId() should be (None)
  }

  test("That userId is None when no app-metadata is present") {
    val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL3NvbWUtZG9tYWluLyIsInN1YiI6Imdvb2dsZS1vYXV0aDJ8MTIzIiwiYXVkIjoiYXNkZmFzZGYiLCJleHAiOjE0ODYwNzAwNjMsImlhdCI6MTQ4NjAzNDA2M30.KEjhvPUooLSFExTrv8XsioJks-NAMzYZjGn32MABvg4"
    val request = mock[HttpServletRequest]
    when(request.getHeader("Authorization")).thenReturn(s"Bearer $token")
    new JWTExtractor(request).extractUserId() should be (None)
  }

  test("That JWTExtractor.extractUserId is set even if roles are not present") {
    val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhcHBfbWV0YWRhdGEiOnsibmRsYV9pZCI6ImFiYzEyMyJ9LCJpc3MiOiJodHRwczovL3NvbWUtZG9tYWluLyIsInN1YiI6Imdvb2dsZS1vYXV0aDJ8MTIzIiwiYXVkIjoiYXNkZmEiLCJleHAiOjE0ODYwNzAwNjMsImlhdCI6MTQ4NjAzNDA2M30.HT3SUUZe52dC4y1FPJb3gpKA5n56WxVM4CmEZDgmpko"
    val request = mock[HttpServletRequest]
    when(request.getHeader("Authorization")).thenReturn(s"Bearer $token")
    val jWTExtractor = new JWTExtractor(request)
    jWTExtractor.extractUserId() should equal (Some("abc123"))
    jWTExtractor.extractUserRoles() should equal(List.empty)
  }

  test("That all roles are extracted") {
    val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhcHBfbWV0YWRhdGEiOnsicm9sZXMiOlsicm9sZTEiLCJyb2xlMiIsInJvbGUzIl0sIm5kbGFfaWQiOiJhYmMxMjMifSwibmFtZSI6IkRvbmFsZCBEdWNrIiwiaXNzIjoiaHR0cHM6Ly9zb21lLWRvbWFpbi8iLCJzdWIiOiJnb29nbGUtb2F1dGgyfDEyMyIsImF1ZCI6ImFiYyIsImV4cCI6MTQ4NjA3MDA2MywiaWF0IjoxNDg2MDM0MDYzfQ.qu8ecEgZiFN8QFL3Jq6SFNL3FMxVvTZTYv7SZ2moyJw"
    val request = mock[HttpServletRequest]
    when(request.getHeader("Authorization")).thenReturn(s"Bearer $token")

    val jwtExtractor = new JWTExtractor(request)
    val roles = jwtExtractor.extractUserRoles()
    roles.size should be (3)
    roles.contains("role1") should be (true)
    roles.contains("role2") should be (true)
    roles.contains("role3") should be (true)
  }

  test("That name is extracted") {
    val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhcHBfbWV0YWRhdGEiOnsicm9sZXMiOlsicm9sZTEiLCJyb2xlMiIsInJvbGUzIl0sIm5kbGFfaWQiOiJhYmMxMjMifSwibmFtZSI6IkRvbmFsZCBEdWNrIiwiaXNzIjoiaHR0cHM6Ly9zb21lLWRvbWFpbi8iLCJzdWIiOiJnb29nbGUtb2F1dGgyfDEyMyIsImF1ZCI6ImFiYyIsImV4cCI6MTQ4NjA3MDA2MywiaWF0IjoxNDg2MDM0MDYzfQ.qu8ecEgZiFN8QFL3Jq6SFNL3FMxVvTZTYv7SZ2moyJw"
    val request = mock[HttpServletRequest]
    when(request.getHeader("Authorization")).thenReturn(s"Bearer $token")

    val jwtExtractor = new JWTExtractor(request)
    val name = jwtExtractor.extractUserName() should equal (Some("Donald Duck"))
  }

}
