package models

import java.util.Date

import play.api.libs.json.Json
import reactivemongo.bson.{BSONDocument, BSONReader}

/**
  * Created by $Jason.Zhang on 10/28/16.
  */
case class Repo(appId:String,repoName:String,createTime:Date)
object Repo{
  implicit val repoJsonReads = Json.reads[Repo]
}
