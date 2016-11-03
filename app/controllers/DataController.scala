package controllers

import javax.inject._

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.source.JsonDocumentSource
import models.Repo
import play.Logger
import play.api.cache.{CacheApi, NamedCache}
import play.api.libs.json.Json
import play.api.mvc._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.play.json._
import services.EsClient

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class DataController @Inject()(val reactiveMongoApi: ReactiveMongoApi,
                               @NamedCache("mongo-cache") cache: CacheApi,
                               esClient: EsClient)
  extends Controller with MongoController with ReactiveMongoComponents {

  /**
    * Create an Action to render an HTML page with a welcome message.
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */

  import play.api.libs.concurrent.Execution.Implicits._

  def collection = database.map(_.collection[reactivemongo.play.json.collection.JSONCollection]("repo"))

  def postData(repoName: String) = Action.async(parse.json) {
    implicit request =>
      val appId = request.headers.get(Constant.AppID)
      val selector = Json.obj(
        "repoName" -> repoName,
        "appId" -> appId
      )
      val cacheKey = repoName + "-" + appId
      val optRepo: Option[Repo] = cache.get(cacheKey)
      if(optRepo.isEmpty){
        Logger.debug("cache miss! get repo info from mongo!")
        collection.flatMap(_.find(selector).one[Repo].map{
          case Some(r) => {
            cache.set(cacheKey,r)
            esClient.client.execute {
              index into r.repoName / (r.repoName + r.appId) doc JsonDocumentSource(request.body.toString())
            }.map {
              s => if (s.isCreated) Ok else InternalServerError("cannot post data")
            }
            Ok
          }
          case _ => NotFound(Json.obj("message"->"not found for required repo info"))
        })
      }else{
        Logger.debug("cache hit!")
        val r = optRepo.get
        esClient.client.execute {
          index into r.repoName / (r.repoName + r.appId) doc JsonDocumentSource(Json.stringify(Json.toJson(r)))
        }.map {
          s => if (s.created) Ok else InternalServerError("cannot post data")
        }
//        val userData: JsResult[JsArray] = request.body.validate[JsArray]
//        val indexRes: Seq[Future[Result]] = userData.asOpt.get.value.map {
//          j =>
//            Logger.debug("to index",j)
//            esClient.client.execute {
//              index into r.repoName / (r.repoName + r.appId) doc JsonDocumentSource(Json.stringify(Json.toJson(j)))
//            }.map {
//              s => if (s.created) Ok else InternalServerError("cannot post data")
//            }
//        }
      }
  }


//  def postDataUsingBulk(repoName: String) = Action.async(parse.json) {
//    implicit request =>
//      val appId = request.headers.get(Constant.AppID)
//      val selector = Json.obj(
//        "repoName" -> repoName,
//        "appId" -> appId
//      )
//      val cacheKey = repoName + "-" + appId
//      val opRepo: Option[Repo] = cache.get(cacheKey)
//      if(opRepo.isEmpty){
//        Logger.debug("cache miss! get repo info from mongo!")
//        collection.flatMap(_.find(selector).one[Repo].map{
//          case Some(r) => {
//            cache.set(cacheKey,r)
//            esClient.client.execute {
//              bulk()
//            }.map {
//              s => if (s.isCreated) Ok else InternalServerError("cannot post data")
//            }
//            Ok
//          }
//          case _ => NotFound(Json.obj("message"->"not found for required repo info"))
//        })
//      }else{
//        Logger.debug("cache hit!")
//        val r = opRepo.get
//        esClient.client.execute {
//          index into r.repoName / (r.repoName + r.appId) doc JsonDocumentSource(request.body.toString)
//        }.map {
//          s => if (s.isCreated) Ok else InternalServerError("cannot post data")
//        }
//      }
//  }

  case class IndexNameAndType(indexName: String, typeName: String)

  //  def getIndexNameAndType(repo:Repo): IndexNameAndType ={
  //
  //  }
}
