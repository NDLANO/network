/*
 * Part of NDLA network.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.network.secrets

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.s3.AmazonS3Client
import org.json4s.native.Serialization.read

import scala.io.Source
import scala.util.{Properties, Success, Try}

object Secrets {
  def readSecrets(secretsFile: String): Try[Map[String, Option[String]]] = {
    val amazonClient = new AmazonS3Client(new DefaultAWSCredentialsProviderChain())
    amazonClient.setRegion(Region.getRegion(Regions.EU_CENTRAL_1))

    new Secrets(amazonClient, Properties.envOrElse("NDLA_ENVIRONMENT", "local"), secretsFile).readSecrets()
  }
}

class Secrets(amazonClient: AmazonS3Client, environment: String, secretsFile: String) {

  def readSecrets(): Try[Map[String, Option[String]]] = {
    implicit val formats = org.json4s.DefaultFormats

    environment match {
      case "local" => Success(Map())
      case _ => {
        for {
          s3Object <- Try(amazonClient.getObject(s"$environment.secrets.ndla", secretsFile))
          fileContent <- Try(Source.fromInputStream(s3Object.getObjectContent).getLines().mkString)
          dbSecrets <- Try(read[Map[String, Option[String]]](fileContent))
        } yield dbSecrets
      }
    }
  }

}

case class Database(database: String,
                    host: String,
                    user: String,
                    password: String,
                    port: String,
                    schema: String)
