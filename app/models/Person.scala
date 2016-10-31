package models

import play.api.libs.json.Json

/**
  * Created by $Jason.Zhang on 10/31/16.
  */

trait Human{
  def name:String
  def age:Int
  def title:String = name
}

case class Person(override val name:String,override val age:Int,createTime:Long = new java.util.Date().getTime) extends Human

object Person{
  implicit val personFormat = Json.format[Person]
}

case class PersonResource(name:String)
object PersonResource{
  implicit val personFormat = Json.format[PersonResource]
}

