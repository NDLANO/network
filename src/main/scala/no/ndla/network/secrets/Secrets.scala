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
import no.ndla.network.secrets.PropertyKeys._
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

object PropertyKeys {
  val MetaUserNameKey = "META_USER_NAME"
  val MetaPasswordKey = "META_PASSWORD"
  val MetaResourceKey = "META_RESOURCE"
  val MetaServerKey = "META_SERVER"
  val MetaPortKey = "META_PORT"
  val MetaSchemaKey = "META_SCHEMA"
}

class Secrets(amazonClient: AmazonS3Client, environment: String, secretsFile: String) {

  def readSecrets(): Try[Map[String, Option[String]]] = {
    implicit val formats = org.json4s.DefaultFormats

    environment match {
      case "local" => Success(Map())
      case _ => {
        val secrets = for {
          s3Object <- Try(amazonClient.getObject(s"$environment.secrets.ndla", secretsFile))
          fileContent <- Try(Source.fromInputStream(s3Object.getObjectContent).getLines().mkString)
          dbSecrets <- Try(read[Database](fileContent))
        } yield dbSecrets

        secrets.map(s => {
          Map(
            MetaResourceKey -> Some(s.database),
            MetaServerKey -> Some(s.host),
            MetaUserNameKey -> Some(s.user),
            MetaPasswordKey -> Some(s.password),
            MetaPortKey -> Some(s.port),
            MetaSchemaKey -> Some(s.schema)
          )
        })
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
