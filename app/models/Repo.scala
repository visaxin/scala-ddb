package models

import java.util.Date

import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json._

/**
  * Created by $Jason.Zhang on 10/28/16.
  */

case class DataPoint(key:String,value:Object)
case class DataPoints(dataPoints: Seq[Map[String,Any]])
case class Schema(key:String,sType:String)

case class FlumeInfo(hosts: List[String], status: Int)

case class ProducerInfo(status: Int, custom: Int, batchSize: Int, taskSize: Int,
                        concurrency: Int, capacity: Int, transcationCapacity: Int,
                        hosts: List[String])

//case class Repo(appId: String,
//                repoName: String,
//                region: String,
//                retention: String,
//                capacity: Int,
//                schema: Seq[Schema],
//                clusterName: String,
//                status: Int,
//                indexVersion: String,
//                templateVersion: String,
//                producerInfo: ProducerInfo,
//                flumeInfo: FlumeInfo,
//                createTime: Date,
//                updateTime: Date)
case class Repo(appId: String,
                region: Option[String],
                schema: List[Map[String,String]],
                repoName: String)

object Repo {
//  implicit val dataPointFormat = Json.format[DataPoint]
//  implicit val dataPointReads = Json.reads[DataPoint]
//  implicit val dataPointsFormat = Json.format[DataPoints]
//  implicit val dataPointsReads = Json.reads[DataPoints]
  implicit val schemaReads = Json.format[Schema]
  implicit val flumeInfoReads = Json.format[FlumeInfo]
  implicit val producerInfo = Json.format[ProducerInfo]
  implicit val repoJsonReads = Json.format[Repo]



  val repoInstance = Json.obj(
    "appId" -> "1",
    "repoName" -> "repoName",
    "region" -> "region",
    "retention" -> "retention",
    "capacity" -> "capacity",
    "schema" -> List(Map("key" -> "value")),
    "clusterName" -> "clusterName",
    "status" -> 1
  )
}

//
//AppID           string         `json:"appID" bson:"app_id"`
//Region          string         `json:"region" bson:"region"`
//RepoName        string         `json:"repoName" bson:"repo_name"`
//Retention       string         `json:"retention" bson:"retention"`
//Capacity        int            `json:"capacity" bson:"capacity"`
//Schema          []SchemaObject `json:"schema" bson:"schema"`
//PortalConfig    PortalConfig   `json:"portalConfig" bson:"portalConfig"`
//RateLimit       RateLimit      `json:"rateLimit" bson:"rateLimit"`
//CreateTime      time.Time      `json:"createTime" bson:"createTime"`
//UpdateTime      time.Time      `json:"updateTime" bson:"updateTime"`
//ClusterName     string         `json:"clusterName" bson:"clusterName"`
//FlumeInfo       FlumeInfo      `json:"flumeInfo" bson:"flumeInfo"`
//ProducerInfo    ProducerInfo   `json:"producerInfo" bson:"producerInfo"`
//Status          int            `json:"status" bson:"status"`                   //repo状态：0，有效；1 无效
//IndexVersion    string         `json:"indexVersion" bson:"indexVersion"`       //index版本戳
//TemplateVersion string         `json:"templateVersion" bson:"templateVersion"` //template版本戳