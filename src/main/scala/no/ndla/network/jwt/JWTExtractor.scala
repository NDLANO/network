/*
 * Part of NDLA network.
 * Copyright (C) 2017 NDLA
 *
 * See LICENSE
 */

package no.ndla.network.jwt

import javax.servlet.http.HttpServletRequest

import no.ndla.network.model.JWTClaims
import pdi.jwt.{JwtJson4s, JwtOptions}

import scala.util.{Failure, Success}


class JWTExtractor(request: HttpServletRequest) {

  private val jwtClaims = Option(request.getHeader("Authorization")).flatMap(authHeader => {
    val jwt = authHeader.replace("Bearer ", "")
    // Leaning on token validation being done somewhere else...
    JwtJson4s.decode(jwt, JwtOptions(signature = false, expiration = false)) match {
      case Success(claims) => Some(JWTClaims(claims))
      case Failure(_) => None
    }
  })

  def extractUserId(): Option[String] = {
    jwtClaims.flatMap(_.ndla_id)
  }

  def extractUserRoles(): List[String] = {
    jwtClaims.map(_.scope).getOrElse(List.empty)
  }

  def extractUserName(): Option[String] = {
    jwtClaims.flatMap(_.user_name)
  }
}
