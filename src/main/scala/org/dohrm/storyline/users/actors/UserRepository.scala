package org.dohrm.storyline.users.actors

import org.dohrm.storyline.users.models.UserDatabase
import org.dohrm.toolkit.actor.DatabaseActor
import org.dohrm.toolkit.context.JdbcConfig

/**
  * @author michaeldohr
  * @since 29/05/16
  */
class UserRepository(implicit override val jdbcConfig: JdbcConfig) extends DatabaseActor with UserDatabase {

  import jdbcConfig.driver.api._

  val tableQueries: TableQuery[Users] = TableQuery[Users]

  override def receive: Receive = {
    case message: Any =>
      sender ! message
  }
}