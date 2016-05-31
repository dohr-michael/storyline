package org.dohrm.storyline.users.actors

import akka.actor.ActorRef
import org.dohrm.storyline.users.models.{User, UserDatabase}
import org.dohrm.toolkit.actor.{DatabaseActor, NotFoundError}
import org.dohrm.toolkit.context.JdbcConfig

import scala.concurrent.ExecutionContext


case object FindAll

case class FindById(id: String)


class UserRepository(implicit override val jdbcConfig: JdbcConfig, ec: ExecutionContext) extends DatabaseActor with UserDatabase {

  import jdbcConfig._
  import driver.api._

  val users: TableQuery[Users] = TableQuery[Users]

  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    // Initialize the database.
    db.run(
      DBIO.seq(
        users.schema.create,
        users += User("dohr.michael@gmail.com", "Michael", "DOHR", "Michael DOHR")
      )
    ).map(_ => println("end init"))
  }

  def findAll: Unit = {
    exec(users.result) { users =>
      users
    }
  }

  def findById(id: String): Unit = {
    exec(
      (for {
        u <- users if u.id === id
      } yield u).result
    ) { users =>
      users.headOption
    }
  }

  def create(user: User): Unit = {
    exec(
      users.forceInsert(user)
    ) { result =>
    }
  }

  override def receive: Receive = {
    case FindAll =>
      findAll
    case FindById(id) =>
      findById(id)
    case message: Any =>
      sender ! message
  }
}