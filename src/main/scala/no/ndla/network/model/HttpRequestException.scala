/*
 * Part of NDLA network.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.network.model

import scalaj.http.HttpResponse

class HttpRequestException(message: String, httpResponse: Option[HttpResponse[String]] = None) extends RuntimeException(message) {
  def is404:Boolean = httpResponse.exists(_.isCodeInRange(404, 404))
}