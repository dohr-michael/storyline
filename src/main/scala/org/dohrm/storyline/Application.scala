package org.dohrm.storyline

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import org.dohrm.toolkit.context.{ActorContext, ConfigContext, FutureContext}
import org.dohrm.storyline.users.UserContext
import org.dohrm.storyline.users.api.CrudUserApi
import org.dohrm.toolkit.utils.{H2Support, PostgresSupport}
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext


trait Routes {
  self: FutureContext
    with ActorContext
    with UserContext
  =>

  import akka.http.scaladsl.server.Directives._

  val crudUserApi = new CrudUserApi(userRepositoryActor)

  val routes: Route =
    path("healthcheck") {
      get {
        complete(HttpEntity(ContentTypes.`application/json`, """{"server":"ready-"}"""))
      }
    } ~
      pathPrefix("users") {
        crudUserApi.routes
      }

}


trait Injector
  extends ActorContext
  with FutureContext
  with ConfigContext {

  override implicit val config: Config = ConfigFactory.load()
  override implicit lazy val as: ActorSystem = ActorSystem("storyline", config)
  override implicit val am: ActorMaterializer = ActorMaterializer()
  override implicit lazy val ec: ExecutionContext = as.dispatcher
  override implicit lazy val actorTimeout: Timeout = Timeout(30, TimeUnit.SECONDS)
}


trait HttpHandler {
  self: ActorContext with FutureContext with ConfigContext with Routes =>

  val logger = Logging(as, this.getClass)

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port")).map { conn =>
    logger.debug(s"Request from ${conn.localAddress}")
    conn
  }.recover {
    case ex => logger.error(s"Request error", ex.getMessage)
  }

}

/**
  * @author michaeldohr
  * @since 13/05/16
  */
object Application
  extends App
  with Injector
  with PostgresSupport
  with UserContext
  with Routes
  with HttpHandler
