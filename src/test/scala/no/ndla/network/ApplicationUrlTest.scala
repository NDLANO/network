/*
 * Part of NDLA network.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.network

import javax.servlet.http.HttpServletRequest

import org.mockito.Mockito._

class ApplicationUrlTest extends UnitSuite {

  val httpRequest = mock[HttpServletRequest]
  val servername = "unittest.testesen.no"
  val scheme = "testscheme"
  val port = 666
  val path = "dette/er/en/test/path"

  override def beforeEach(): Unit = {
    reset(httpRequest)
    when(httpRequest.getServerName).thenReturn(servername)
    when(httpRequest.getScheme).thenReturn(scheme)
    when(httpRequest.getServerPort).thenReturn(port)
    when(httpRequest.getServletPath).thenReturn(path)
  }

  test("That applicationUrl returns default wnen header is not defined") {
    ApplicationUrl.set(httpRequest)
    ApplicationUrl.get should equal(s"${scheme}://${servername}:${port}${path}/")
  }

  test("That applicationUrl returns http wnen header is not defined and port is 80") {
    when(httpRequest.getScheme).thenReturn("http")
    when(httpRequest.getServerPort).thenReturn(80)
    ApplicationUrl.set(httpRequest)
    ApplicationUrl.get should equal(s"http://${servername}${path}/")
  }

  test("That applicationUrl returns https wnen header is not defined and port is 443") {
    when(httpRequest.getScheme).thenReturn("https")
    when(httpRequest.getServerPort).thenReturn(443)
    ApplicationUrl.set(httpRequest)
    ApplicationUrl.get should equal(s"https://${servername}${path}/")
  }

  test("That applicationUrl returns http when header is http") {
    when(httpRequest.getHeader(ApplicationUrl.ProtocolHeader)).thenReturn("http")
    ApplicationUrl.set(httpRequest)
    ApplicationUrl.get should equal(s"http://${servername}${path}/")
  }

  test("That applicationUrl returns https when header is https") {
    when(httpRequest.getHeader(ApplicationUrl.ProtocolHeader)).thenReturn("https")
    ApplicationUrl.set(httpRequest)
    ApplicationUrl.get should equal(s"https://${servername}${path}/")
  }

  test("That https header trumps http port") {
    when(httpRequest.getHeader(ApplicationUrl.ProtocolHeader)).thenReturn("https")
    ApplicationUrl.set(httpRequest)
    when(httpRequest.getServerPort).thenReturn(80)
    ApplicationUrl.get should equal(s"https://${servername}${path}/")
  }

  test("That http header trumps https port") {
    when(httpRequest.getHeader(ApplicationUrl.ProtocolHeader)).thenReturn("http")
    ApplicationUrl.set(httpRequest)
    when(httpRequest.getServerPort).thenReturn(443)
    ApplicationUrl.get should equal(s"http://${servername}${path}/")
  }

  test("That applicationUrl returns default when header is unrecognized") {
    when(httpRequest.getHeader(ApplicationUrl.ProtocolHeader)).thenReturn("tullogtoys")
    ApplicationUrl.set(httpRequest)
    ApplicationUrl.get should equal(s"${scheme}://${servername}:${port}${path}/")
  }
}
