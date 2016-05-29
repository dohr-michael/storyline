package org.dohrm.toolkit.context

import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend.DatabaseDef

/**
  * @author michaeldohr
  * @since 29/05/16
  */
trait JdbcContext {

  implicit val driver: JdbcProfile

  implicit def db: DatabaseDef
}
