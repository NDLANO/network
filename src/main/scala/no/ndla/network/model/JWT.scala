/*
 * Part of NDLA network.
 * Copyright (C) 2017 NDLA
 *
 * See LICENSE
 */

package no.ndla.network.model

import org.json4s.native.JsonMethods.parse
import pdi.jwt.JwtClaim


case class JWTClaims(
                      iss: Option[String],
                      sub: Option[String],
                      aud: Option[Set[String]],
                      azp: Option[String],
                      exp: Option[Long],
                      iat: Option[Long],
                      scope: List[String],
                      ndla_id: Option[String],
                      user_name: Option[String],
                      jti: Option[String],
                      client_id: Option[String]
                    )

object JWTClaims {
  implicit val formats = org.json4s.DefaultFormats
  val ndla_id_key = "https://ndla.no/ndla_id"
  val user_name_key = "https://ndla.no/user_name"
  val client_id_key = "https://ndla.no/client_id"
  val azp_key = "azp"
  val scope_key = "scope"

  def apply(claims: JwtClaim): JWTClaims = {
    val content = {
      if (claims.content.contains("\"app_metadata\":")) {
        // Legacy format. May be discarded when all clients are migrated to new auth0 auth using access tokens (instead of id tokens)
        legacyTokenParse(claims.content)
      } else {
        parse(claims.content).extract[Map[String, String]]
      }
    }
    new JWTClaims(claims.issuer, claims.subject, claims.audience, content.get(azp_key), claims.expiration, claims.issuedAt, content.get(scope_key).map(_.split(' ').toList).getOrElse(List.empty), content.get(ndla_id_key), content.get(user_name_key), claims.jwtId, content.get(client_id_key))
  }

  def legacyTokenParse(content: String) = {
    val legacyClientIdValueMarkers = List("client", "netlife", "frontend")
    val legacyContent = parse(content).extract[Map[String, Any]]

    def conditionalEntry(key: String, value: Option[String]) = value match {
      case Some(v) => Seq(key -> v)
      case None => Nil
    }

    def getLegacyRoles = {
      legacyContent.get("app_metadata").map(_.asInstanceOf[Map[String, Any]]) match {
        case Some(appMetaData) => appMetaData.get("roles").map(_.asInstanceOf[List[String]]).map(_.mkString(" "))
        case None => None
      }
    }

    def getLegacyNdlaOrClientId: Option[String] = {
      legacyContent.get("app_metadata").map(_.asInstanceOf[Map[String, Any]]) match {
        case Some(appMetaData) => appMetaData.get("ndla_id").map(_.asInstanceOf[String])
        case None => None
      }
    }

    def getLegacyNdlaId = getLegacyNdlaOrClientId.filter(x => !legacyClientIdValueMarkers.exists(y => x.indexOf(y) > 0))

    def getLegacyClientId = getLegacyNdlaOrClientId.filter(x => legacyClientIdValueMarkers.exists(y => x.indexOf(y) > 0))

    def getLegacyUserName = legacyContent.get("name").map(_.asInstanceOf[String])

    val contentInNewFormat = Seq(conditionalEntry(azp_key, Some("uknown")) ++ conditionalEntry(scope_key, getLegacyRoles) ++ conditionalEntry(ndla_id_key, getLegacyNdlaId) ++ conditionalEntry(user_name_key, getLegacyUserName) ++ conditionalEntry(client_id_key, getLegacyClientId))
    Map(contentInNewFormat.flatten: _*)
  }

}
