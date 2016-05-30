package org.dohrm.toolkit.actor

import akka.actor.{Actor, ActorRef}
import org.dohrm.toolkit.context.JdbcContext
import slick.jdbc.JdbcBackend

/**
  * @author michaeldohr
  * @since 29/05/16
  */
trait DatabaseActor extends Actor with JdbcContext {

  implicit def dbSession: JdbcBackend.SessionDef = jdbcConfig.db.createSession()

}

object DatabaseActor {

  case class Request(origin: ActorRef, name: String, params: (String, Any)*)

}