package org.dohrm.storyline.users.actors

import org.dohrm.storyline.users.models.{User, UserDatabase}
import org.dohrm.toolkit.actor.JdbcActor
import org.dohrm.toolkit.actor.response.{InvalidRequestError, Response}
import org.dohrm.toolkit.context.JdbcConfig
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext


case object FindAll

case class FindById(id: String)

case class Create(user: User)

case class Update(id: String, user: User)

case class Delete(id: String)

class UserRepository(implicit override val jdbcConfig: JdbcConfig, ec: ExecutionContext) extends JdbcActor with UserDatabase {

  import jdbcConfig._
  import driver.api._

  implicit val users: TableQuery[Users] = TableQuery[Users]

  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    // Initialize the database.
    db.run(
      DBIO.seq(
        users.schema.create,
        users += User("dohr.michael@gmail.com", "Michael", "DOHR", "Michael DOHR", DateTime.now, None)
      )
    ).map(_ => println("end init"))
  }

  def findAll: Unit = {
    execAndForwardToSender(
      users.result
    )(users => Response(users))
  }

  def findById(id: String): Unit = {
    execAndForwardToSender(
      (for {
        u <- users if u.id === id
      } yield u).result
    )(users => Response.ofOpt(users.headOption))
  }

  def create(user: User): Unit = {
    println(user)
    execAndForwardToSender {
      (users returning users) += user
    }(result => Response(result))
  }

  override def receive: Receive = {
    case FindAll =>
      findAll
    case FindById(id) =>
      findById(id)
    case Create(user) =>
      create(user)
    case message: Any =>
      sender ! message
    case _ =>
      sender ! Response.failed(InvalidRequestError(Seq("unknown")))
  }
}