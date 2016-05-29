package org.dohrm.toolkit.utils

import org.dohrm.toolkit.context.{ConfigContext, JdbcContext}
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend
import slick.jdbc.JdbcBackend.DatabaseDef

/**
  * @author michaeldohr
  * @since 29/05/16
  */
trait H2Support extends JdbcContext {
  self: ConfigContext =>

  private val DbConfig = config.getConfig("h2")

  private lazy val lazyDb = JdbcBackend.Database.forURL(
    url = s"jdbc:h2://${DbConfig.getString("url")};${DbConfig.getString("options")}"
  )

  override implicit def db: DatabaseDef = lazyDb

  override implicit val driver: JdbcProfile = slick.driver.H2Driver

}
