package org.dohrm.toolkit.actor

import akka.actor.{Actor, ActorRef}
import org.dohrm.toolkit.context.JdbcContext
import slick.dbio.NoStream
import slick.jdbc.JdbcBackend

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author michaeldohr
  * @since 29/05/16
  */
trait DatabaseActor extends Actor with JdbcContext {

  import jdbcConfig._
  import jdbcConfig.driver.api._

  implicit def dbSession: JdbcBackend.SessionDef = jdbcConfig.db.createSession()

  def exec[A, B](action: DBIOAction[A, NoStream, Nothing])(mapper: A => B)(implicit ec: ExecutionContext): Unit = {
    val previousSender = sender
    db.run(action)
      .map(mapper)
      .recover {
        case e => ExceptionError(e)
      }
      .foreach {
        case None => previousSender ! NotFoundError()
        case Some(item) => previousSender ! item
        case item => previousSender ! item
      }
  }

}

object DatabaseActor {

  case class Request(origin: ActorRef, name: String, params: (String, Any)*)

}