package controllers

import java.util.Date
import javax.inject._

import com.sksamuel.elastic4s.ElasticDsl._
import db.PostMongoRepo
import models.Repo
import play.Logger
import play.api.libs.json.Json
import play.api.mvc._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.bson.BSONDocument
import reactivemongo.core.actors.Exceptions.PrimaryUnavailableException
import services.EsClient

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class RepoController @Inject()(
                                val reactiveMongoApi: ReactiveMongoApi)
  extends Controller with MongoController with ReactiveMongoComponents {

  /**
    * Create an Action to render an HTML page with a welcome message.
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  def postRepo = new PostMongoRepo(reactiveMongoApi)
  import play.api.libs.concurrent.Execution.Implicits._
  import controllers.RepoFields._

  def list = Action.async { implicit request =>
    postRepo.find()
      .map(posts => Ok(Json.toJson(posts.reverse)))
      .recover { case PrimaryUnavailableException => InternalServerError("Please install MongoDB") }
  }

  def add = Action.async(BodyParsers.parse.json) { implicit request =>
    val appId = (request.body \ AppId).as[String]
    val repoName = (request.body \ RepoName).as[String]
    val createTime = new Date()
    postRepo.save(BSONDocument(
      AppId -> appId,
      repoName -> repoName,
      CreateTime -> createTime
    )).map(le => Redirect(routes.RepoController.list()))

  }
}

object RepoFields {
  val Id = "_id"
  val AppId = "appId"
  val RepoName = "repoName"
  val CreateTime = ""
}
