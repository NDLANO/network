/*
 * Part of NDLA network.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.network.secrets

import java.io.ByteArrayInputStream

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.{S3Object, S3ObjectInputStream}
import no.ndla.network.UnitSuite
import org.apache.http.client.methods.HttpRequestBase
import org.mockito.Matchers._
import org.mockito.Mockito._

import scala.util.{Failure, Success}

class SecretsTest extends UnitSuite {

  val amazonClient = mock[AmazonS3Client]
  val s3Object = mock[S3Object]

  val ValidFileContent = """ {
                             "database": "database-name",
                             "host": "database-host",
                             "user": "database-user",
                             "password": "database-password",
                             "port": "1234",
                             "schema": "database-schema"
                           }""".stripMargin

  override def beforeEach(): Unit = {
    reset(amazonClient, s3Object)
    when(amazonClient.getObject(anyString(), anyString())).thenReturn(s3Object)
  }

  test("That empty Map is returned when env is local") {
    new Secrets(amazonClient, "local", "secretsFile").readSecrets() should equal (Success(Map()))
  }

  test("That Map containing details about database is returned when env is test") {
    when(s3Object.getObjectContent).thenReturn(new S3ObjectInputStream(new ByteArrayInputStream(ValidFileContent.getBytes("UTF-8")), mock[HttpRequestBase]))
    val secretsAttempt = new Secrets(amazonClient, "test", "secretsFile").readSecrets()

    secretsAttempt match {
      case Failure(err) => fail(err)
      case Success(secrets) => {
        secrets("user") should equal (Some("database-user"))
        secrets("password") should equal (Some("database-password"))
        secrets("database") should equal (Some("database-name"))
        secrets("host") should equal (Some("database-host"))
        secrets("port") should equal (Some("1234"))
        secrets("schema") should equal (Some("database-schema"))
      }
    }
  }

  test("That Failure is returned when not able to parse secret content") {
    when(s3Object.getObjectContent).thenReturn(new S3ObjectInputStream(new ByteArrayInputStream("ThouShaltNotParse".getBytes("UTF-8")), mock[HttpRequestBase]))
    new Secrets(amazonClient, "test", "secretsFile").readSecrets().isFailure should be (true)
  }

  test("That Failure is returned when Amazon-problems occur") {
    when(s3Object.getObjectContent).thenThrow(new RuntimeException("AmazonProblem"))
    new Secrets(amazonClient, "test", "secretsFile").readSecrets().isFailure should be (true)
  }
}
