package org.dohrm.toolkit.json

import org.joda.time.DateTime
import spray.json.{JsString, JsValue, RootJsonFormat}

import scala.util.{Failure, Success, Try}

/**
  * @author michaeldohr
  * @since 03/06/16
  */
case class InvalidFormatException(message: String, cause: Throwable = null) extends RuntimeException(message, cause)


trait Formats {

  implicit def transform[A](obj: A)(implicit fmt: RootJsonFormat[A]): JsValue = fmt.write(obj)

  implicit lazy val dateTimeFormat: RootJsonFormat[DateTime] = new RootJsonFormat[DateTime] {

    private val Iso8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

    override def read(json: JsValue): DateTime = {
      Try(
        DateTime.parse(json.asInstanceOf[JsString].value)
      ) match {
        case Success(date: DateTime) => date
        case Failure(ex) => throw new InvalidFormatException("invalid_date_format", ex)
      }
    }

    override def write(obj: DateTime): JsValue = {
      JsString(obj.toString(Iso8601))
    }
  }
}

object Formats extends Formats
