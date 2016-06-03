package org.dohrm.storyline.users

import java.util.concurrent.TimeUnit

import akka.actor.Props
import akka.pattern.BackoffSupervisor
import org.dohrm.storyline.users.actors.UserRepository
import org.dohrm.toolkit.context.{ActorContext, FutureContext, JdbcContext}

import scala.concurrent.duration.FiniteDuration


trait UserContext {
  self: ActorContext
    with FutureContext
    with JdbcContext
  =>

  val userRepositoryActor = as.actorOf(
    BackoffSupervisor.props(
      Props(new UserRepository),
      "crud-user",
      FiniteDuration(3, TimeUnit.SECONDS),
      FiniteDuration(30, TimeUnit.SECONDS),
      0.42
    )
  )


}
