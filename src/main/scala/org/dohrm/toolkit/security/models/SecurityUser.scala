package org.dohrm.toolkit.security.models


trait SecurityUser[ID] {
  def id: ID
  def grants: Seq[String]
}
