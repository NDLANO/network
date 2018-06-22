/*
 * Part of NDLA network.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.network

import javax.servlet.http.HttpServletRequest
import no.ndla.network.model.NdlaHttpRequest

object ApplicationUrl {
  val X_FORWARDED_PROTO_HEADER = "X-Forwarded-Proto"
  val FORWARDED_HEADER = "Forwarded"
  val FORWARDED_PROTO = "proto"
  val HTTP = "http"
  val HTTPS = "https"
  val HTTP_PORT = 80
  val HTTPS_PORT = 443

  val applicationUrl = new ThreadLocal[String]

  def set(request: HttpServletRequest): Unit = set(NdlaHttpRequest(request))

  def set(request: NdlaHttpRequest) {
    val xForwardedProtoHeaderProtocol = request.getHeader(X_FORWARDED_PROTO_HEADER)
    val forwardedHeaderProtocol = request.getHeader(FORWARDED_HEADER).flatMap(_.replaceAll("\\s","").split(";").find(_.contains(FORWARDED_PROTO)).map(_.dropWhile(c => c != '=').tail))
    val schemeProtocol = if (request.serverPort == HTTP_PORT || request.serverPort == HTTPS_PORT) Some(request.getScheme) else None

    val chosenProtocol = List(forwardedHeaderProtocol, xForwardedProtoHeaderProtocol, schemeProtocol).find(x => x.isDefined && (x.get.equals(HTTP) || x.get.equals(HTTPS))).flatten

    if (chosenProtocol.isDefined)
      applicationUrl.set(s"${chosenProtocol.get}://${request.serverName}${request.servletPath}/")
    else
      applicationUrl.set(s"${request.getScheme}://${request.serverName}:${request.serverPort}${request.servletPath}/")
  }

  def get: String = applicationUrl.get

  def clear(): Unit = applicationUrl.remove()
}
