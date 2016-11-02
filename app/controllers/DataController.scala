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
      var r  = cache.get(repoName + appId)
      if (r.isEmpty) {
        collection.flatMap(_.find(selector).one[Repo]).map {
          case Some(s) => r = s
          case None => NotFound
        }
        cache.set(repoName + appId, r)
        Logger.debug("found from from")
      } else {
        r = cache.get(repoName + appId).asInstanceOf[Repo]
        Logger.debug("found from cache")

      }
      esClient.client.execute {
        index into r.repoName / (r.repoName + r.appId) doc JsonDocumentSource(request.body.toString())
      }.map {
        s => if (s.isCreated) Ok else InternalServerError("cannot post data")
      }
    //
    //      repoInstance.map {
    //        case Some(ri) =>
    //          val r = ri.asInstanceOf[Repo]
    //          esClient.client.execute {
    //            index into r.repoName / (r.repoName + r.appId) doc JsonDocumentSource(request.body.toString())
    //          }.map {
    //            s => if (s.isCreated) Ok else InternalServerError("cannot post data")
    //          }
    //          Ok
    //        case _ => InternalServerError
    //      }


    //      repo.map{
    //        case Some(r) => {
    //          esClient.client.execute {
    //            index into r.repoName / (r.repoName + r.appId) doc JsonDocumentSource(request.body.toString())
    //          }.map{
    //            s => if(s.isCreated) Ok else InternalServerError("cannot post data")
    //          }
    //          Ok
    //        }
    //        case None =>
    //          Logger.debug("not found!!")
    //
    //          NotFound(Json.obj("message" -> "not found"))
    //        case _ =>
    //          InternalServerError
    //      }
  }


  case class IndexNameAndType(indexName: String, typeName: String)

  //  def getIndexNameAndType(repo:Repo): IndexNameAndType ={
  //
  //  }
}
