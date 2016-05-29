package org.dohrm.storyline.users.models

import org.dohrm.toolkit.context.JdbcContext
import spray.json.RootJsonFormat

/**
  * User
  * @param id user id (correspond to the email.)
  * @param firstName user first name.
  * @param lastName user last name.
  * @param displayName user display name
  */
case class User(id: String, firstName: String, lastName: String, displayName: String)

/**
  * User metadata.
  */
object UserJson {

  import spray.json.DefaultJsonProtocol._

  val format: RootJsonFormat[User] = jsonFormat4(User)
}


trait UserDatabase {
  self: JdbcContext =>

  import driver.api._

  class Users(tag: Tag) extends Table[User](tag, "USERS") {
    def id = column[String]("USER_ID", O.PrimaryKey)

    def firstName = column[String]("USER_FIRST_NAME")

    def lastName = column[String]("USER_LAST_NAME")

    def displayName = column[String]("USER_DISPLAY_NAME")

    def * = (id, firstName, lastName, displayName) <>(User.tupled, User.unapply)
  }

}
