package zio2.https2.example

import zio.logging.LogFormat
import zio.logging.backend.SLF4J
import zio.{Scope, ULayer, ZIO, ZIOAppArgs, ZIOAppDefault, ZLayer}
import zio2.https2.example.Types.Requirements
import zio2.https2.example.api.ServerHttp4s
import zio2.https2.example.config.Configuration
import zio2.https2.example.db.PostgresDatabase
import zio2.https2.example.service.{HealthCheckService, ProductCommandService, ProductQueryService}

object Application extends ZIOAppDefault:

  private val appLayer: ULayer[Requirements] = ZLayer.make[Requirements](
    Scope.default,
    Configuration.fullConfigLayer,
    Configuration.httpConfigLayer,
    Configuration.dbConfigLayer,
    Configuration.appConfigLayer,
    PostgresDatabase.transactorLive,
    HealthCheckService.live,
    ProductQueryService.live,
    ProductCommandService.live
  )

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Unit] =
    zio.Runtime.removeDefaultLoggers >>> SLF4J.slf4j(LogFormat.line + LogFormat.cause)

  override def run: ZIO[ZIOAppArgs & Scope, Any, Any] =
    ZIO.logInfo("Application start") *> ServerHttp4s.run
      .provideLayer(appLayer)
      .tapError(error => ZIO.logError(s"Error $error"))
      .tapDefect(throwable => ZIO.logError(s"Defect: $throwable"))
      .exitCode
  end run

end Application

