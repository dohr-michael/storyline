package org.dohrm.toolkit.json

import spray.json.{JsObject, JsValue, RootJsonFormat, RootJsonReader}

/**
  * @author michaeldohr
  * @since 03/06/16
  */
trait JsonEnrichment {

  def default(params: (String, JsValue)*): RootJsonReader[JsValue] =
    new RootJsonReader[JsValue] {
      override def read(json: JsValue): JsValue = JsObject(params.toMap ++ json.asJsObject.fields)
    }

  def force(params: (String, JsValue)*): RootJsonReader[JsValue] =
    new RootJsonReader[JsValue] {
      override def read(json: JsValue): JsValue = JsObject(json.asJsObject.fields ++ params.toMap)
    }

  def prune(fields: String*): RootJsonReader[JsValue] =
    new RootJsonReader[JsValue] {
      override def read(json: JsValue): JsValue = JsObject(fields.foldLeft(json.asJsObject.fields)((acc, c) => acc - c))
    }

  def move(fields: (String, String)*): RootJsonReader[JsValue] =
    new RootJsonReader[JsValue] {
      override def read(json: JsValue): JsValue = JsObject(
        fields.foldLeft(json.asJsObject.fields) { (acc, c) =>
          acc.get(c._1).fold(acc) { value =>
            (acc + (c._2 -> value)) - c._1
          }
        }
      )
    }

  implicit class FunctionalEnrichment(format: RootJsonReader[JsValue]) {
    def andThen(newOne: RootJsonReader[JsValue]): RootJsonReader[JsValue] = {
      new RootJsonReader[JsValue] {
        override def read(json: JsValue): JsValue = newOne.read(format.read(json))
      }
    }
  }

  implicit class JSEnrichment[A](format: RootJsonFormat[A]) {

    def beforeRead(writer: RootJsonReader[JsValue]): RootJsonFormat[A] = {
      new RootJsonFormat[A] {
        override def write(obj: A): JsValue =
          format.write(obj)

        override def read(json: JsValue): A =
          format.read(writer.read(json))
      }
    }

    def afterWrite(writer: RootJsonReader[JsValue]): RootJsonFormat[A] = {
      new RootJsonFormat[A] {
        override def write(obj: A): JsValue =
          writer.read(format.write(obj))

        override def read(json: JsValue): A =
          format.read(json)
      }
    }

  }

}

object JsonEnrichment extends JsonEnrichment
