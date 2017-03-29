/*
 * Part of NDLA network.
 * Copyright (C) 2017 NDLA
 *
 * See LICENSE
 */

package no.ndla.network

import javax.servlet.http.HttpServletRequest

import com.typesafe.scalalogging.LazyLogging
import no.ndla.network.jwt.JWTExtractor


object AuthUser extends LazyLogging {

  private val userId = new ThreadLocal[Option[String]]
  private val userRoles = new ThreadLocal[List[String]]
  private val userName = new ThreadLocal[Option[String]]
  private val authHeader = new ThreadLocal[String]

  def set(request: HttpServletRequest): Unit = {
    val jWTExtractor = new JWTExtractor(request)
    userId.set(jWTExtractor.extractUserId())
    userRoles.set(jWTExtractor.extractUserRoles())
    userName.set(jWTExtractor.extractUserName())
    authHeader.set(request.getHeader("Authorization"))
  }

  def get: Option[String] = userId.get
  def getRoles: List[String] = userRoles.get
  def getName: Option[String] = userName.get
  def getHeader: Option[String] = Option(authHeader.get)

  def hasRole(role: String): Boolean = getRoles.contains(role)

  def clear(): Unit = {
    userId.remove()
    userRoles.remove()
    userName.remove()
    authHeader.remove()
  }
}

