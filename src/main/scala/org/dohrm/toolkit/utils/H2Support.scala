package org.dohrm.toolkit.utils

import com.github.tototoshi.slick.{H2JodaSupport, GenericJodaSupport}
import org.dohrm.toolkit.context.{ConfigContext, JdbcConfig, JdbcContext}
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

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

  override implicit lazy val jdbcConfig: JdbcConfig = new JdbcConfig {

    override val jodaSupport: GenericJodaSupport = H2JodaSupport

    override def db: JdbcBackend.DatabaseDef = lazyDb

    override val driver: JdbcProfile = slick.driver.H2Driver
  }

}
