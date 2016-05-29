package org.dohrm.storyline.users

import java.util.concurrent.TimeUnit

import akka.actor.Props
import akka.pattern.BackoffSupervisor
import org.dohrm.toolkit.context.{ActorContext, JdbcContext}
import org.dohrm.storyline.users.actors.{UserRepository, CrudUser}

import scala.concurrent.duration.FiniteDuration


trait UserContext {
  self: ActorContext
    with JdbcContext
  =>

  lazy val userRepositoryActor = as.actorOf(Props[UserRepository])

  lazy val userActor = as.actorOf(
    BackoffSupervisor.props(
      Props[CrudUser],
      "crud-user",
      FiniteDuration(3, TimeUnit.SECONDS),
      FiniteDuration(30, TimeUnit.SECONDS),
      0.42
    )
  )


}
