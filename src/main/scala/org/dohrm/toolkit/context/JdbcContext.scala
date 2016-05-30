package org.dohrm.toolkit.context

import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend.DatabaseDef

/**
  * @author michaeldohr
  * @since 29/05/16
  */
trait JdbcConfig {

  val driver: JdbcProfile

  def db: DatabaseDef
}

trait JdbcContext {

  implicit val jdbcConfig: JdbcConfig

}
