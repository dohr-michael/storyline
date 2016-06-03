package org.dohrm.toolkit.actor

import akka.actor.Actor
import org.dohrm.toolkit.actor.response.Response
import org.dohrm.toolkit.context.JdbcContext
import slick.jdbc.JdbcBackend

import scala.concurrent.{ExecutionContext, Future}

trait JdbcActor
  extends Actor with JdbcContext {

  import jdbcConfig._
  import driver.api._

  implicit def dbSession: JdbcBackend.SessionDef = jdbcConfig.db.createSession()

  def exec[A, B](action: DBIOAction[A, NoStream, Nothing])(mapper: A => Response[B])(implicit ec: ExecutionContext): Future[Response[B]] = {
    db.run(action)
      .map(mapper)
  }

  def forwardToSender[A](f: Future[Response[A]])(implicit ec: ExecutionContext): Unit = {
    val previousSender = sender
    f.recover(
      (exceptionToErrorMapper orElse defaultExceptionToErrorMapper) andThen { e => Response.failed(e) }
    ).foreach { r =>
      previousSender ! r
    }
  }

  def execAndForwardToSender[A, B](action: DBIOAction[A, NoStream, Nothing])(mapper: A => Response[B])(implicit ec: ExecutionContext): Unit = {
    forwardToSender(
      exec(action)(mapper)
    )
  }


}