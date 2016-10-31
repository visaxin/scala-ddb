package v1.repo

import javax.inject._

import db.PostMongoRepo
import models.{Person, PersonResource}
import play.Logger
import play.api.libs.json._
import play.api.mvc._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.play.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class RepoController @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends Controller
  with MongoController with ReactiveMongoComponents {

  /**
    * Create an Action to render an HTML page with a welcome message.
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  def collection = database.map(_.collection[reactivemongo.play.json.collection.JSONCollection]("repo"))

  //  def collection = db.collection[JSONCollection]("repo")

  def index = Action.async {

//    val repoList = collection.flatMap(_.find(Json.obj()).cursor[Person]().collect[List]())
    def cl =  new PostMongoRepo(reactiveMongoApi)
    val repoList = cl.find()
    val repoArray = repoList.map {
      repo =>
        Json.arr(repo)
    }
    repoArray.map {
      re => Ok(re)
    }
  }

  def create(name: String, age: Int) = Action.async {
    val json = Json.obj(
      "name" -> name,
      "age" -> age,
      "createTime" -> new java.util.Date().getTime
    )

    val ps = Person(name, age)
    collection.flatMap(_.insert(ps).map {
      _ => Ok
    }.recover {
      case _ => InternalServerError
    }
    )
  }

  def find(name: String) = Action.async {
    val json = Json.obj(
      "name" -> name
    )
    val repoList = collection.flatMap(_.find(json).cursor[Person]().collect[List]())

    val repoArray = repoList.map {
      repo =>
        Json.arr(repo)
    }

    repoArray.map {
      re => Ok(re)
    }
  }

  def findByAge(age: Int) = Action.async {
    val json = Json.obj(
      "age" -> age
    )
    val repoList = collection.flatMap(_.find(json).cursor[PersonResource]().collect[List]())

    val repoArray = repoList.map {
      repo =>
        Json.arr(repo)
    }

    repoArray.map {
      re => Ok(re)
    }
  }

  def createByBody = Action.async(parse.json) {
    implicit request => {
      request.body.validate[Person].map {
        p =>
          collection.flatMap(_.insert(p).map {
            lastError =>
              Logger.debug(s"create person ok! $lastError")
              Created
          })
      }.getOrElse(Future.successful(BadRequest("invalid json")))
    }
  }


}
