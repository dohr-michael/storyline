package org.dohrm.storyline.users.actors

import akka.actor.ActorLogging
import org.dohrm.storyline.users.models.{User, UserDatabase, UserWithGrants}
import org.dohrm.toolkit.actor.response.Response
import org.dohrm.toolkit.actor.{JdbcActor, ModelRepositoryActor}
import org.dohrm.toolkit.context.JdbcConfig

import scala.concurrent.ExecutionContext


case class GetWithGrants(user: User)

class UserRepository(implicit override val jdbcConfig: JdbcConfig, ec: ExecutionContext)
  extends ModelRepositoryActor[User, String, UserWithGrants]
    with JdbcActor
    with UserDatabase
    with ActorLogging {

  import jdbcConfig._
  import driver.api._

  implicit val users: TableQuery[Users] = TableQuery[Users]


  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    db.run(
      DBIO.seq(
        users.schema.create
      )
    ).map(_ => println("end init user"))
  }

  override protected def all(implicit su: Option[UserWithGrants]): Unit = {
    execAndForwardToSender(
      users.result
    )(users => Response(users))
  }

  override protected def one(id: String)(implicit su: Option[UserWithGrants]): Unit = {
    execAndForwardToSender(
      (for {
        u <- users if u.id === id
      } yield u).result
    )(users => Response.ofOpt(users.headOption))
  }

  override protected def create(user: User)(implicit su: Option[UserWithGrants]): Unit = {
    execAndForwardToSender {
      (users returning users) += user
    }(result => Response(result))
  }

  override protected def update(id: String, entity: User)(implicit su: Option[UserWithGrants]): Unit = {
    // forwardToRepository(Update(id, entity))
  }

  override protected def delete(id: String)(implicit su: Option[UserWithGrants]): Unit = {
    // forwardToRepository(Delete(id))
  }

  protected def getWithGrants(user: User): Unit = {
    // TODO write upsert in database.
    sender ! Response.unit(UserWithGrants.from(user))
  }

  override def receive: Receive =
    super.receive orElse {
      case GetWithGrants(user) =>
        getWithGrants(user)
    }
}
