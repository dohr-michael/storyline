package org.dohrm.toolkit.actor.response

trait Error

case object UnknownError extends Error

case class ExceptionError(e: Throwable) extends Error

case class NotFoundError(messages: Seq[String] = Seq.empty) extends Error

case class InvalidRequestError(messages: Seq[String] = Seq.empty) extends Error


