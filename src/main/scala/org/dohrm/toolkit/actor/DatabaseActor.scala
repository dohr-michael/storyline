package org.dohrm.toolkit.actor

import akka.actor.{Actor, ActorRef}
import org.dohrm.toolkit.context.JdbcContext
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend
import slick.jdbc.JdbcBackend.DatabaseDef

/**
  * @author michaeldohr
  * @since 29/05/16
  */
abstract class DatabaseActor(override val driver: JdbcProfile, val db: DatabaseDef) extends Actor with JdbcContext {

  implicit def dbSession: JdbcBackend.SessionDef = db.createSession()

}

object DatabaseActor {

  case class Request(origin: ActorRef, name: String, params: (String, Any)*)

}