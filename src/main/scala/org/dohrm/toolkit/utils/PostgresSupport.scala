package org.dohrm.toolkit.utils

import com.github.tototoshi.slick.{PostgresJodaSupport, GenericJodaSupport}
import org.dohrm.toolkit.actor.response.{InvalidRequestError, ExceptionError, Error}
import org.dohrm.toolkit.context.{ConfigContext, JdbcConfig, JdbcContext}
import org.postgresql.util.PSQLException
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

    override val jodaSupport: GenericJodaSupport = PostgresJodaSupport

    override def db: JdbcBackend.DatabaseDef = lazyDb

    override val driver: JdbcProfile = slick.driver.PostgresDriver

    override def exceptionToErrorMapper: PartialFunction[Throwable, Error] = {
      case e: PSQLException if e.getServerErrorMessage.getSQLState == "23505"=>
        InvalidRequestError(Seq(s"${e.getServerErrorMessage.getTable.toLowerCase}.unique_violation"))
      case e: PSQLException =>
        println(e.getServerErrorMessage)
        ExceptionError(e)
    }
  }

}
