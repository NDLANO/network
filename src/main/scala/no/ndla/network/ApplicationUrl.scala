/*
 * Part of NDLA network.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.network

import javax.servlet.http.HttpServletRequest

object ApplicationUrl {
  val ProtocolHeader = "X-Forwarded-Proto"
  val HTTP = "http"
  val HTTPS = "https"
  val HTTP_PORT = 80
  val HTTPS_PORT = 443

  val applicationUrl = new ThreadLocal[String]

  def set(request: HttpServletRequest) {
    val protocolHeader = request.getHeader(ProtocolHeader)
    if (protocolHeader != null && (protocolHeader.equals(HTTP) || protocolHeader.equals(HTTPS)))
      applicationUrl.set(s"${protocolHeader}://${request.getServerName}${request.getServletPath}/")
    else if (request.getServerPort == HTTP_PORT || request.getServerPort == HTTPS_PORT)
      applicationUrl.set(s"${request.getScheme}://${request.getServerName}${request.getServletPath}/")
    else
      applicationUrl.set(s"${request.getScheme}://${request.getServerName}:${request.getServerPort}${request.getServletPath}/")
  }

  def get: String = applicationUrl.get

  def clear(): Unit = applicationUrl.remove()
}
