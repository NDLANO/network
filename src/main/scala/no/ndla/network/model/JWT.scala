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
    @deprecated("Will be removed in one of the next releases")
    client_id: Option[String]
)

object JWTClaims {
  implicit val formats = org.json4s.DefaultFormats
  private val ndla_id_key = "https://ndla.no/ndla_id"
  private val user_name_key = "https://ndla.no/user_name"
  private val client_id_key = "https://ndla.no/client_id"
  private val azp_key = "azp"
  private val scope_key = "scope"

  def apply(claims: JwtClaim): JWTClaims = {
    val content = parse(claims.content).extract[Map[String, String]]
    new JWTClaims(
      claims.issuer,
      claims.subject,
      claims.audience,
      content.get(azp_key),
      claims.expiration,
      claims.issuedAt,
      content.get(scope_key).map(_.split(' ').toList).getOrElse(List.empty),
      content.get(ndla_id_key),
      content.get(user_name_key),
      claims.jwtId,
      content.get(client_id_key)
    )
  }
}
