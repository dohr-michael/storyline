package org.dohrm.toolkit.actor.response

import org.dohrm.toolkit.actor.response

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}


object Response {
  def unit[A](answer: A): Response[A] = Positive(answer)

  def withExplanation[A](answer: A, explanation: Explanation): Response[A] = Positive(answer, Option(explanation))

  def failed[A](cause: response.Error): Response[A] = Negative(cause)

  def ofOpt[A](opt: Option[A]): Response[A] = opt.fold(failed[A](NotFoundError()))(unit)

  def sequence[A](serviceResults: Seq[Response[A]]): Response[Seq[A]] = serviceResults.foldLeft(Response.unit(Seq.empty[A])){ (acc, cur) =>
    for {
      a <- acc
      c <- cur
    } yield {
      a :+ c
    }
  }

  def apply[A](value: => A): Response[A] = {
    Try(value) match {
      case Success(t) => Positive(t)
      case Failure(e) => Negative(ExceptionError(e))
    }
  }

}

sealed trait Response[A] {
  def cause: Option[response.Error]

  def isNegation: Boolean = cause.isDefined

  def get: A

  final lazy val getAsOpt: Option[A] = cause.fold[Option[A]](Some(get))(_ => None)

  final def asOpt: Option[A] = getAsOpt

  final def getOrElse[B >: A](default: B): B = cause.fold[B](get)(_ => default)

  def map[B](f: A => B): Response[B]

  /**
    * Applies the function to the underlying object. Doesn't apply if Negative.
    * @param f the function to apply
    */
  def flatMap[B](f: A => Response[B]): Response[B] = cause.fold[Response[B]](f(get))(e => Negative(e))

  /**
    * Applies fn to the underlying object and if no object, returns Default
    * @param fn the function to apply
    * @param default the default value
    */
  def fold[B](default: response.Error => B)(fn: A => B): B = {
    cause.fold(fn(get))(e => default(e))
  }

  /**
    * Applies fn to the underlying object. fn returns a Future.
    * @param fn the function to apply
    */
  def flatMapF[B](fn: A => Future[Response[B]]): Future[Response[B]] = {
    cause.fold[Future[Response[B]]](fn(get))(e => Future.successful(Negative(e)))
  }

  /**
    * Applies fn to the underlying object. fn returns a Future.
    * @param fn the function to apply
    */
  def mapF[B](fn: A => Future[B])(implicit ec: ExecutionContext): Future[Response[B]] = {
    cause.fold[Future[Response[B]]](fn(get).map(b => map(_ => b)))(e => Future.successful(Negative(e)))
  }

  /**
    * Zip two Responses
    * @param other the other Responses
    * @tparam B the type of the other Response
    * @return zipped Response
    */
  def zip[B](other: Response[B]): Response[(A, B)] = flatMap(m => other.map(m -> _))

  /**
    * Filters the Response
    * @param p the predicate
    * @return filtered Response
    */
  def filter(p: A => Boolean): Response[A] = if (cause.isDefined || p(get)) this else Negative(NotFoundError())


  /**
    * Collects the service result
    * @param pf the partialfunction
    * @tparam B the type of return
    * @return collected Response
    */
  def collect[B](pf: PartialFunction[A, B]): Response[B] = filter(pf.isDefinedAt).map(pf.apply)

  /**
    * Flattens the two Responses
    * @param ev evidence
    * @tparam B type of the evidence
    * @return flattened Response
    */
  def flatten[B](implicit ev: A <:< Response[B]): Response[B] = cause.fold(ev(get))(e => Response.failed(e))
}

case class Positive[A](answer: A, explanation: Option[Explanation] = None) extends Response[A] {
  override lazy val cause: Option[response.Error] = None

  override lazy val get: A = answer

  final override def map[B](f: (A) => B): Response[B] = Positive(f(get), explanation)
}

case class Negative[A](errorCause: response.Error) extends Response[A] {
  override lazy val cause: Option[response.Error] = Some(errorCause)

  final def get: Nothing = throw new NoSuchElementException("Negative.get")

  final override def map[B](f: (A) => B): Response[B] = Negative(errorCause)
}