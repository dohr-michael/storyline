package org.dohrm.toolkit.utils

import org.dohrm.toolkit.context.{ConfigContext, JdbcConfig, JdbcContext}
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

/**
  * @author michaeldohr
  * @since 29/05/16
  */
trait PostgresSupport extends JdbcContext {
  self: ConfigContext =>

  private val DbConfig = config.getConfig("postgres")

  private lazy val lazyDb = JdbcBackend.Database.forURL(
    url = s"jdbc:postgresql://${DbConfig.getString("url")}:${DbConfig.getString("port")}/${DbConfig.getString("database")}",
    user = DbConfig.getString("user"),
    password = DbConfig.getString("password"),
    driver = DbConfig.getString("driver"),
    keepAliveConnection = DbConfig.getBoolean("keepAliveConnection")
  )

  override implicit lazy val jdbcConfig: JdbcConfig = new JdbcConfig {
    override def db: JdbcBackend.DatabaseDef = lazyDb

    override val driver: JdbcProfile = slick.driver.PostgresDriver
  }

}
