package db

import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.ReadPreference
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import reactivemongo.play.json._, collection._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}


trait PostRepo {
  def find()(implicit ec: ExecutionContext): Future[List[JsObject]]

//  def find(selector: reactivemongo.play.json.collection.JSONCollection)(implicit ec: ExecutionContext): Future[List[JsObject]]
  def find(selector: BSONDocument)(implicit ec: ExecutionContext): Future[List[JsObject]]

  // default ascending
  def find(selector: BSONDocument, sortKey: Option[String], sortWay: Int = 1)(implicit ec: ExecutionContext): Future[List[JsObject]]

  def update(selector: BSONDocument, update: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult]

  def remove(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult]

  def save(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult]
}

class PostMongoRepo(reactiveMongoApi: ReactiveMongoApi) extends PostRepo {
  // BSON-JSON conversions
  import play.modules.reactivemongo.json._

  protected def collection =
    reactiveMongoApi.database.map(_.collection[JSONCollection]("repo"))

  //  protected def collection =
  //    reactiveMongoApi.db.collection[JSONCollection]("repo")

  def find()(implicit ec: ExecutionContext): Future[List[JsObject]] =
    collection.flatMap(_.find(Json.obj()).cursor[JsObject](ReadPreference.Primary).collect[List]())

  def update(selector: BSONDocument, update: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult] =
    collection.flatMap(_.update(selector, update))

  def remove(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult] =
    collection.flatMap(_.remove(document))

  def save(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult] =
    collection.flatMap(_.update(BSONDocument("_id" -> document.get("_id").getOrElse(BSONObjectID.generate)), document, upsert = true))

//  def find(selector: reactivemongo.play.json.collection.JSONCollection)(implicit ec: ExecutionContext): Future[List[JsObject]] =
//    collection.flatMap(_.find(selector).cursor[JsObject](ReadPreference.Primary).collect[List]())
  def find(selector: BSONDocument)(implicit ec: ExecutionContext): Future[List[JsObject]] =
    collection.flatMap(_.find(selector).cursor[JsObject](ReadPreference.Primary).collect[List]())

  // default ascending
  def find(selector: BSONDocument, sortKey: Option[String], sortWay: Int = 1)(implicit ec: ExecutionContext): Future[List[JsObject]] = {
    val sort = Json.obj(
      sortKey.getOrElse("_id") -> sortWay
    )
    collection.flatMap(_.find(selector).sort(sort).cursor[JsObject](ReadPreference.Primary).collect[List]())
  }

}

object PostRepo{
}


