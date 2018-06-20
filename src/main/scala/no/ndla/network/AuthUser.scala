/*
 * Part of NDLA network.
 * Copyright (C) 2017 NDLA
 *
 * See LICENSE
 */

package no.ndla.network

import javax.servlet.http.HttpServletRequest

import no.ndla.network.jwt.JWTExtractor


object AuthUser {
  private[this] def ThreadLocalWithDefault[T](default: T) = new ThreadLocal[T] {
    override protected def initialValue: T = default
  }

  private val userId = ThreadLocalWithDefault[Option[String]](None)
  private val userRoles = ThreadLocalWithDefault[List[String]](List.empty)
  private val userName = ThreadLocalWithDefault[Option[String]](None)
  private val clientId = ThreadLocalWithDefault[Option[String]](None)
  private val authHeader = ThreadLocalWithDefault[String]("")

  def set(request: HttpServletRequest): Unit = {
    val jWTExtractor = new JWTExtractor(request)
    setId(jWTExtractor.extractUserId())
    setRoles(jWTExtractor.extractUserRoles())
    setName(jWTExtractor.extractUserName())
    setClientId(jWTExtractor.extractClientId())
    setHeader(request.getHeader("Authorization"))
  }

  def setId(user: Option[String]): Unit = userId.set(user)
  def setRoles(roles: List[String]): Unit = userRoles.set(roles)
  def setName(name: Option[String]): Unit = userName.set(name)
  def setClientId(client: Option[String]): Unit = clientId.set(client)
  def setHeader(header: String): Unit = authHeader.set(header)

  def get: Option[String] = userId.get
  def getRoles: List[String] = userRoles.get
  def getName: Option[String] = userName.get
  def getClientId: Option[String] = clientId.get
  def getHeader: Option[String] = Option(authHeader.get)

  def hasRole(role: String): Boolean = getRoles.contains(role)

  def clear(): Unit = {
    userId.remove()
    userRoles.remove()
    userName.remove()
    clientId.remove()
    authHeader.remove()
  }
}

