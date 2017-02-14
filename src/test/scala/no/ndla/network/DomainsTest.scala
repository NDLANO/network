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

class DomainsTest extends UnitSuite {

  test("That local env returns localhost") {
    Domains.get("local") should equal("http://proxy.ndla-local")
  }

  test("That prod env returns prod") {
    Domains.get("prod") should equal("https://api.ndla.no")
  }

  test("That ant other env returns any other env") {
    Domains.get("anyotherenv") should equal("http://anyotherenv.api.ndla.no")
  }
}
