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
                      jti: Option[String]
                    )

object JWTClaims {
  implicit val formats = org.json4s.DefaultFormats

  def apply(claims: JwtClaim): JWTClaims = {
    implicit val formats = org.json4s.DefaultFormats
    val content = parse(claims.content).extract[Map[String, String]]
    new JWTClaims(claims.issuer, claims.subject, claims.audience, content.get("azp"), claims.expiration, claims.issuedAt, content.get("scope").map(_.split(' ').toList).getOrElse(List.empty), content.get("https://ndla.no/ndla_id"), content.get("https://ndla.no/user_name"), claims.jwtId)
  }
}
