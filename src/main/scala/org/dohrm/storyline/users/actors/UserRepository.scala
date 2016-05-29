package org.dohrm.storyline.users.actors

import org.dohrm.toolkit.actor.DatabaseActor
import org.dohrm.storyline.users.models.{User, UserDatabase}
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend.DatabaseDef

/**
  * @author michaeldohr
  * @since 29/05/16
  */
class UserRepository(implicit override val driver: JdbcProfile, db: DatabaseDef) extends DatabaseActor(driver, db) with UserDatabase {
  import driver.api._
  import UserRepository._

  val tableQueries: TableQuery[Users] = TableQuery[Users]

  override def receive: Receive = {
    case DatabaseActor.Request(sender, All) =>
  }
}

object UserRepository {

  val All = "ALL"
}