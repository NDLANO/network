/*
 * Part of NDLA network.
 * Copyright (C) 2017 NDLA
 *
 * See LICENSE
 */

package no.ndla.network.jwt

import javax.servlet.http.HttpServletRequest

import authentikat.jwt.JsonWebToken
import no.ndla.network.model.JWTClaims
import org.json4s.native.Serialization.read

import scala.util.{Failure, Success, Try}


class JWTExtractor(request: HttpServletRequest) {

  implicit val formats = org.json4s.DefaultFormats

  private val jwtClaims = Option(request.getHeader("Authorization")).flatMap(authHeader => {
    val jwt = authHeader.replace("Bearer ", "")
    jwt match {
      case JsonWebToken(header, claimsSet, signature) => {
        Try(read[JWTClaims](claimsSet.asJsonString)) match {
          case Success(claims) => Some(claims)
          case Failure(_) => None
        }
      }
      case _ => None
    }
  })

  def extractUserId(): Option[String] = {
    jwtClaims.flatMap(x => x.app_metadata).map(_.ndla_id)
  }

  def extractUserRoles(): List[String] = {
    jwtClaims.flatMap(_.app_metadata).map(_.roles).getOrElse(List.empty)
  }

  def extractUserName(): Option[String] = {
    jwtClaims.flatMap(_.name)
  }
}
