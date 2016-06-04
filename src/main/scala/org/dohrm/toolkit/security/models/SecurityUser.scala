package org.dohrm.toolkit.security.models


trait SecurityUser {
  def id: String
  def grants: Seq[String]
}
