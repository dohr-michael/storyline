package org.dohrm.storyline.users.models

import org.dohrm.toolkit.context.JdbcContext
import org.dohrm.toolkit.security.models.SecurityUser
import org.joda.time.DateTime
import spray.json.RootJsonFormat


case class User(id: String,
                displayName: String,
                picture: String,
                locale: String,
                creationDate: DateTime = DateTime.now,
                lastConnectionDate: Option[DateTime] = None)

case class UserWithGrants(id: String,
                          displayName: String,
                          picture: String,
                          locale: String,
                          creationDate: DateTime = DateTime.now,
                          lastConnectionDate: Option[DateTime] = None,
                          grants: Seq[String] = Seq.empty) extends SecurityUser[String]

object UserWithGrants {

  def from(user: User, grants: Seq[String] = Seq.empty): UserWithGrants =
    UserWithGrants(user.id, user.displayName, user.picture, user.locale, user.creationDate, user.lastConnectionDate, grants)

}

/**
  * User metadata.
  */
object UserJson {

  import org.dohrm.toolkit.json.Formats._
  import org.dohrm.toolkit.json.JsonEnrichment._
  import spray.json.DefaultJsonProtocol._

  /**
    * Format.
    */
  implicit val userFormat: RootJsonFormat[User] =
    jsonFormat6(User.apply)
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

    def displayName = column[String]("USER_DISPLAY_NAME")

    def picture = column[String]("USER_PICTURE")

    def locale = column[String]("USER_LOCALE")

    def creationDate = column[DateTime]("USER_CREATION_DATE")

    def lastUpdateDate = column[Option[DateTime]]("USER_LAST_UPDATE_DATE")

    def * = (id, displayName, picture, locale, creationDate, lastUpdateDate) <>(User.tupled, User.unapply)
  }

}
