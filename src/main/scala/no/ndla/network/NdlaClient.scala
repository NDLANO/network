/*
 * Part of NDLA network.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.network

import no.ndla.network.model.HttpRequestException
import org.json4s.jackson.JsonMethods._

import scala.util.{Failure, Success, Try}
import scalaj.http.{HttpRequest, HttpResponse}

trait NdlaClient {
  val ndlaClient: NdlaClient

  class NdlaClient {
    implicit val formats = org.json4s.DefaultFormats

    def fetch[A](request: HttpRequest, user: Option[String] = None, password: Option[String] = None)(implicit mf: Manifest[A]): Try[A] = {
      val requestWithCorrelationId = CorrelationID.get match {
        case None => request
        case Some(correlationId) => request.header("X-Correlation-ID", correlationId)
      }

      val requestWithAuthHeader = AuthUser.getHeader match {
        case Some(auth) => request.header("Authorization", auth)
        case None => requestWithCorrelationId
      }

      val requestWithBasicAuth = if (user.isDefined && password.isDefined) {
        requestWithAuthHeader.auth(user.get, password.get)
      } else requestWithAuthHeader

      for {
        httpResponse <- doRequest(requestWithBasicAuth)
        bodyObject <- parseResponse[A](httpResponse)(mf)
      } yield bodyObject
    }

    private def doRequest(request: HttpRequest): Try[HttpResponse[String]] = {
      Try(request.asString).flatMap(response => {
        response.isError match {
          case false => Success(response)
          case true => {
            Failure(new HttpRequestException(s"Received error ${response.code} ${response.statusLine} when calling ${request.url}", Some(response)))
          }
        }
      })
    }

    private def parseResponse[A](response: HttpResponse[String])(implicit mf: Manifest[A]): Try[A] = {
      Try(parse(response.body).camelizeKeys.extract[A]) match {
        case Success(extracted) => Success(extracted)
        case Failure(ex) => Failure(new HttpRequestException(s"Could not parse response ${response.body}", Some(response)))
      }
    }
  }

}
