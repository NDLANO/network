/*
 * Part of NDLA network.
 * Copyright (C) 2017 NDLA
 *
 * See LICENSE
 */

package no.ndla.network

import javax.servlet.http.HttpServletRequest

import org.mockito.Mockito.{reset, when}

class AuthUserTest extends UnitSuite {

  test("That AuthUser.get is None when no authorization header is set"){
    AuthUser.set(mock[HttpServletRequest])
    AuthUser.get should be (None)
  }

  test("That AuthUser.get is None when an illegal JWT is set") {
    val request = mock[HttpServletRequest]
    when(request.getHeader(AuthUser.AuthorizationHeader)).thenReturn("This is an invalid JWT")

    AuthUser.set(request)
    AuthUser.get should be (None)
  }

  test("That AuthUser.get is None when no app-metadata is present") {
    val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2tlc2tub3cuZXUuYXV0aDAuY29tLyIsInN1YiI6Imdvb2dsZS1vYXV0aDJ8MTE0Mzc3NzU4MTkzNDMzOTQ4MTI5IiwiYXVkIjoiYkNadFgwRkllYVRHTExqTXRNbzlJQk85VlRnZUd6eXYiLCJleHAiOjE0ODYwNzAwNjMsImlhdCI6MTQ4NjAzNDA2M30.-X8eIlLBGbRq6NHIcdEJY4WhvzvpUH7lhQcLd8ub_K4"
    val request = mock[HttpServletRequest]
    when(request.getHeader(AuthUser.AuthorizationHeader)).thenReturn(s"Bearer $token")
    AuthUser.set(request)
    AuthUser.get should be (None)
  }

  test("That AuthUser.get is set even if roles are not present") {
    val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhcHBfbWV0YWRhdGEiOnsibmRsYV9pZCI6ImFiYzEyMyJ9LCJpc3MiOiJodHRwczovL2tlc2tub3cuZXUuYXV0aDAuY29tLyIsInN1YiI6Imdvb2dsZS1vYXV0aDJ8MTE0Mzc3NzU4MTkzNDMzOTQ4MTI5IiwiYXVkIjoiYkNadFgwRkllYVRHTExqTXRNbzlJQk85VlRnZUd6eXYiLCJleHAiOjE0ODYwNzAwNjMsImlhdCI6MTQ4NjAzNDA2M30.ZbUH8eA9K6vp4MNisDLyFZcEm76jhweFa7hMzhVO6iQ"
    val request = mock[HttpServletRequest]
    when(request.getHeader(AuthUser.AuthorizationHeader)).thenReturn(s"Bearer $token")
    AuthUser.set(request)
    AuthUser.get should equal (Some("abc123"))
    AuthUser.getRoles should equal(List.empty)
  }

  test("That all roles are extracted") {
    val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhcHBfbWV0YWRhdGEiOnsicm9sZXMiOlsicm9sZTEiLCJyb2xlMiIsInJvbGUzIl0sIm5kbGFfaWQiOiJhYmMxMjMifSwiaXNzIjoiaHR0cHM6Ly9rZXNrbm93LmV1LmF1dGgwLmNvbS8iLCJzdWIiOiJnb29nbGUtb2F1dGgyfDExNDM3Nzc1ODE5MzQzMzk0ODEyOSIsImF1ZCI6ImJDWnRYMEZJZWFUR0xMak10TW85SUJPOVZUZ2VHenl2IiwiZXhwIjoxNDg2MDcwMDYzLCJpYXQiOjE0ODYwMzQwNjN9.1v_aIMyyntWjM--vkwilsPyO4QnHrYA2jdAweiEfE5A"
    val request = mock[HttpServletRequest]
    when(request.getHeader(AuthUser.AuthorizationHeader)).thenReturn(s"Bearer $token")
    AuthUser.set(request)
    AuthUser.getRoles.size should be (3)
    AuthUser.getRoles.contains("role1") should be (true)
    AuthUser.getRoles.contains("role2") should be (true)
    AuthUser.getRoles.contains("role3") should be (true)
  }
}
