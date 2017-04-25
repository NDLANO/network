/*
 * Part of NDLA network.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.network

import com.typesafe.scalalogging.LazyLogging
import no.ndla.network.model.HttpRequestException
import org.json4s.jackson.JsonMethods._

import scala.util.{Failure, Success, Try}
import scalaj.http.{HttpRequest, HttpResponse}

trait NdlaClient {
  val ndlaClient: NdlaClient

  class NdlaClient extends LazyLogging {
    implicit val formats = org.json4s.DefaultFormats


    def fetch[A](request: HttpRequest)(implicit mf: Manifest[A]): Try[A] = {
      doFetch(
        addCorrelationId(request))
    }

    def fetchWithBasicAuth[A](request: HttpRequest, user: String, password: String)(implicit mf: Manifest[A]): Try[A] = {
      doFetch(
        addCorrelationId(
          addBasicAuth(request, user, password)))
    }

    def fetchWithForwardedAuth[A](request: HttpRequest)(implicit mf: Manifest[A]): Try[A] = {
      doFetch(
        addCorrelationId(
          addForwardedAuth(request)))
    }

    private def doFetch[A](request: HttpRequest)(implicit mf: Manifest[A]): Try[A] = {
      for {
        httpResponse <- doRequest(request)
        bodyObject <- parseResponse[A](httpResponse)(mf)
      } yield bodyObject
    }

    private def doRequest(request: HttpRequest): Try[HttpResponse[String]] = {
      Try(request.asString).flatMap(response => {
        response.isError match {
          case false => Success(response)
          case true => {
            Failure(new HttpRequestException(s"Received error ${response.code} ${response.statusLine} when calling ${request.url}. Body was ${response.body}", Some(response)))
          }
        }
      })
    }

    private def parseResponse[A](response: HttpResponse[String])(implicit mf: Manifest[A]): Try[A] = {
      Try(parse(response.body).camelizeKeys.extract[A]) match {
        case Success(extracted) => Success(extracted)
        case Failure(ex) => {
          logger.warn("Could not parse response", ex)
          Failure(new HttpRequestException(s"Could not parse response ${response.body}", Some(response)))
        }
      }
    }

    private def addCorrelationId(request: HttpRequest) = CorrelationID.get match {
      case None => request
      case Some(correlationId) => request.header("X-Correlation-ID", correlationId)
    }

    private def addBasicAuth(request: HttpRequest, user: String, password: String) = request.auth(user, password)

    private def addForwardedAuth(request: HttpRequest) = AuthUser.getHeader match {
      case Some(auth) => request.header("Authorization", auth)
      case None => request
    }
  }
}
