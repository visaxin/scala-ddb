package services

import javax.inject.{Inject, Singleton}

import com.sksamuel.elastic4s.ElasticClient
import org.elasticsearch.common.settings.Settings
import play.api.inject.ApplicationLifecycle

import scala.concurrent.Future

/**
  * Created by $Jason.Zhang on 10/28/16.
  */
@Singleton
class EsClient @Inject()(lifeCycle: ApplicationLifecycle) {


  val esSet = Settings.settingsBuilder()
    .put("cluster.name", "cluster")
    .put("path.home", "/tmp")
    .build()

  val singletonClient = ElasticClient.local(esSet)
  lifeCycle.addStopHook {
    () =>
      Future.successful(singletonClient.close())
  }

  def client = singletonClient
}

