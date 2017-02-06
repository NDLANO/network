/*
 * Part of NDLA network.
 * Copyright (C) 2017 NDLA
 *
 * See LICENSE
 */

package no.ndla.network

import javax.servlet.http.HttpServletRequest

import authentikat.jwt.JsonWebToken
import com.typesafe.scalalogging.LazyLogging
import no.ndla.network.model.AuthorizationException
import org.json4s.native.Serialization.read

import scala.util.{Failure, Success, Try}


object AuthUser extends LazyLogging {
  val AuthorizationHeader = "Authorization"

  private val userId = new ThreadLocal[Option[String]]
  private val userRoles = new ThreadLocal[List[String]]

  def set(request: HttpServletRequest): Unit = {
    extractClaims(request) match {
      case Success(claims) =>
        userId.set(extractUserId(claims))
        userRoles.set(extractUserRoles(claims))

      case Failure(err) =>
        logger.info(err.getMessage, err)
        userId.set(None)
        userRoles.set(List.empty)
    }
  }

  def get: Option[String] = userId.get
  def getRoles: List[String] = userRoles.get

  def clear(): Unit = {
    userId.remove()
    userRoles.remove()
  }

  private def extractClaims(request: HttpServletRequest): Try[JWTClaims] = {
    implicit val formats = org.json4s.DefaultFormats

    val optAuthHeader = Option(request.getHeader(AuthorizationHeader))
    optAuthHeader.map(authHeader => {
      val jwt = authHeader.replace("Bearer ", "")
      jwt match {
        case JsonWebToken(header, claimsSet, signature) => {
          Try(read[JWTClaims](claimsSet.asJsonString))
        }
        case _ => Failure(new AuthorizationException("Invalid Authorization header found"))
      }
    }).getOrElse(Failure(new AuthorizationException("No Authorization-header found")))
  }

  private def extractUserId(jwtClaims: JWTClaims): Option[String] = {
    jwtClaims.app_metadata.map(_.ndla_id)
  }

  private def extractUserRoles(jwtClaims: JWTClaims): List[String] = {
    jwtClaims.app_metadata.map(_.roles).getOrElse(List.empty)
  }

  private case class JWTClaims(
                                iss: Option[String],
                                sub: Option[String],
                                aud: Option[String],
                                exp: Option[Long],
                                iat: Option[Long],
                                app_metadata: Option[JWTAppMetadata])

  private case class JWTAppMetadata(ndla_id: String, roles: List[String])

}

