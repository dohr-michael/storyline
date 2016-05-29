package org.dohrm.toolkit.actor

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object Response {

  type Response[A] = Either[Error, A]

  implicit class EitherEnricher[A](result: Response[A]) {

    def map[B](fn: A => B): Response[B] = result.fold(e => Left(e), a => Right(fn(a)))

    def mapF[B](fn: A => B)(implicit ec: ExecutionContext): Future[Response[B]] = Future.successful(map(fn))

    def flatMap[B](fn: A => Response[B]): Response[B] = result.fold(e => Left(e), a => fn(a))

    def flatMapF[B](fn: A => Future[Response[B]])(implicit ec: ExecutionContext): Future[Response[B]] =
      result.fold(e => Future.successful(Left(e)), a => fn(a))

    def foreach(fn: A => Unit): Unit = result.fold(_ => Unit, e => fn(e))
  }


  def unit[A](value: A): Response[A] = Right(value)

  def unitF[A](value: A): Future[Response[A]] = Future.successful(unit(value))

  def failed[A](error: Error): Response[A] = Left(error)

  def failedF[A](error: Error): Future[Response[A]] = Future.successful(failed(error))

  def ofOpt[A](opt: Option[A]): Response[A] = opt.fold(failed[A](NotFoundError()))(unit)

  def sequence[A](serviceResponses: Seq[Response[A]]): Response[Seq[A]] = serviceResponses.foldLeft(Response.unit(Seq.empty[A])) { (acc, cur) =>
    for {
      a <- acc
      c <- cur
    } yield {
      a :+ c
    }
  }

  def sequenceF[A](futureServiceResponses: Seq[Future[Response[A]]])(implicit ec: ExecutionContext): Future[Response[Seq[A]]] =
    Future.sequence(futureServiceResponses).map { serviceResponses =>
      sequence(serviceResponses)
    }

  def apply[A](value: => A): Response[A] = {
    Try(value) match {
      case Success(t) => Right(t)
      case Failure(e) => Left(ExceptionError(e))
    }
  }

  def ofFuture[A](value: Future[A])(implicit ec: ExecutionContext): Future[Response[A]] = value.map(Response.unit)

  def ofFutureOpt[A](value: Future[Option[A]])(implicit ec: ExecutionContext): Future[Response[A]] = value.map(Response.ofOpt)

  implicit class FutureServiceResponseOps[A](future: Future[Response[A]]) {
    def mapR[B](fn: A => Response[B])(implicit ec: ExecutionContext): Future[Response[B]] = future.map(sr => sr.flatMap(fn))

    def flatMapR[B](fn: A => Future[Response[B]])(implicit ec: ExecutionContext): Future[Response[B]] = future.flatMap(sr => sr.flatMapF(fn))
  }

}

trait Error

case object UnknownError extends Error

case class ExceptionError(e: Throwable) extends Error

case class NotFoundError(messages: Seq[String] = Seq.empty) extends Error

case class InvalidRequestError(messages: Seq[String] = Seq.empty) extends Error


