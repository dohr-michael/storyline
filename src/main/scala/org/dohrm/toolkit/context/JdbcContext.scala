package org.dohrm.toolkit.context

import com.github.tototoshi.slick.GenericJodaSupport
import org.dohrm.toolkit.actor.response.{ExceptionError, Error}
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend.DatabaseDef

/**
  * @author michaeldohr
  * @since 29/05/16
  */
trait JdbcConfig {

  val jodaSupport: GenericJodaSupport
  val driver: JdbcProfile

  def db: DatabaseDef

  def exceptionToErrorMapper: PartialFunction[Throwable, Error] = PartialFunction.empty

  final def defaultExceptionToErrorMapper: PartialFunction[Throwable, Error] = {
    case e => ExceptionError(e)
  }
}

trait JdbcContext {

  implicit val jdbcConfig: JdbcConfig
}
