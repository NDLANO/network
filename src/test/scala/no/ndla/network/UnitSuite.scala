/*
 * Part of NDLA network.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.network

import org.scalatest._
import org.scalatestplus.mockito.MockitoSugar

import scala.util.Properties

abstract class UnitSuite
    extends FunSuite
    with Matchers
    with OptionValues
    with Inside
    with Inspectors
    with MockitoSugar
    with BeforeAndAfterAll
    with BeforeAndAfterEach {

  def withEnv(key: String, value: Option[String])(toDoWithEnv: => Any) = {
    val originalEnv = Properties.envOrNone(key)

    value match {
      case Some(envValue) => setEnv(key, envValue)
      case None           => rmEnv(key)
    }

    toDoWithEnv

    originalEnv match {
      case Some(original) => setEnv(key, original)
      case None           => rmEnv(key)
    }
  }

  private def setEnv(key: String, value: String) = {
    val field = System.getenv().getClass.getDeclaredField("m")
    field.setAccessible(true)
    val map = field.get(System.getenv()).asInstanceOf[java.util.Map[java.lang.String, java.lang.String]]
    map.put(key, value)
  }

  private def rmEnv(key: String) = {
    val field = System.getenv().getClass.getDeclaredField("m")
    field.setAccessible(true)
    val map = field.get(System.getenv()).asInstanceOf[java.util.Map[java.lang.String, java.lang.String]]
    map.remove(key)
  }
}
