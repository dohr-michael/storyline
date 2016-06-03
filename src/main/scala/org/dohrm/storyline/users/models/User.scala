package org.dohrm.storyline.users.models

import org.dohrm.toolkit.context.JdbcContext
import org.joda.time.DateTime
import spray.json.RootJsonFormat

/**
  * User
  *
  * @param id          user id (correspond to the email.)
  * @param firstName   user first name.
  * @param lastName    user last name.
  * @param displayName user display name
  */
case class User(id: String,
                firstName: String,
                lastName: String,
                displayName: String,
                creationDate: DateTime = DateTime.now,
                lastConnectionDate: Option[DateTime] = None)

/**
  * User metadata.
  */
object UserJson {

  import spray.json.DefaultJsonProtocol._
  import org.dohrm.toolkit.json.Formats._
  import org.dohrm.toolkit.json.JsonEnrichment._

  /**
    * Format.
    */
  implicit val userFormat: RootJsonFormat[User] =
    jsonFormat6(User)
      .beforeRead(default("creationDate" -> DateTime.now))
}


trait UserDatabase {
  self: JdbcContext =>

  import jdbcConfig._
  import driver.api._
  import jodaSupport._


  /**
    * User database request
    */
  class Users(tag: Tag) extends Table[User](tag, "USERS") {

    def id = column[String]("USER_ID", O.PrimaryKey)

    def firstName = column[String]("USER_FIRST_NAME")

    def lastName = column[String]("USER_LAST_NAME")

    def displayName = column[String]("USER_DISPLAY_NAME")

    def creationDate = column[DateTime]("USER_CREATION_DATE")

    def lastUpdateDate = column[Option[DateTime]]("USER_LAST_UPDATE_DATE")

    def * = (id, firstName, lastName, displayName, creationDate, lastUpdateDate) <>(User.tupled, User.unapply)
  }

}
