package zio2.https2.example.config

import pureconfig.*
import pureconfig.error.ConfigReaderFailures
import pureconfig.generic.derivation.default.*
import zio.*
import zio2.https2.example.errors.AppError

import scala.concurrent.duration.FiniteDuration

object Configuration:

  private val configSource = ConfigSource.default

  val fullConfigLayer: ULayer[AppEnvironmentConfig] = ZLayer.fromZIO {
    (
      for
        _ <- ZIO.logDebug("Constructing full config layer")
        config <- ZIO
          .fromEither(configSource.load[AppEnvironmentConfig])
          .mapError(e => AppError.validation("Cannot read config: " + e.toString))
        _ <- validate(config)
      yield config
      ).catchAll(e => ZIO.die(e.toThrowable))
  }

  val httpConfigLayer: URLayer[AppEnvironmentConfig, HttpConfig] = ZLayer.fromZIO {
    ZIO.logDebug("Constructing HTTP config layer") *> ZIO.service[AppEnvironmentConfig].map(_.http)
  }

  val dbConfigLayer: URLayer[AppEnvironmentConfig, DbConfig] = ZLayer.fromZIO {
    for {
      config <- ZIO.service[AppEnvironmentConfig]
      _ <- ZIO.logInfo(s"Constructing DB config layer: ${config.db}")
    } yield config.db
  }

  val appConfigLayer: URLayer[AppEnvironmentConfig, AppConfig] = ZLayer.fromZIO {
    ZIO.logDebug("Constructing App config layer") *> ZIO.service[AppEnvironmentConfig].map(_.app)
  }

  private def validate(config: AppEnvironmentConfig): IO[AppError, Unit] =
    ZIO
      .fail(AppError.validation("Error validating config: nearly expired duration cannot be negative"))
      .when(config.app.nearlyExpired.toMillis < 0)
      .unit
end Configuration

final case class AppEnvironmentConfig(http: HttpConfig, db: DbConfig, app: AppConfig)derives ConfigReader

final case class HttpConfig(host: String, port: Int, healthCheckConfig: HealthCheckConfig)

final case class HealthCheckConfig(postgresTimeoutSeconds: Int)

final case class DbConfig(
                           dataSource: DbDataSource,
                           connectionTimeout: Int,
                           minimumIdle: Int,
                           maximumPoolSize: Int
                         ):
  def user: String = dataSource.user

  def password: String = dataSource.password

  def jdbcUrl: String =
    s"jdbc:postgresql://${dataSource.host}:${dataSource.port}/${dataSource.databaseName}"

  def driver: String = dataSource.driver
end DbConfig

final case class DbDataSource(
                               host: String,
                               port: Int,
                               user: String,
                               password: String,
                               databaseName: String,
                               driver: String
                             )

final case class AppConfig(nearlyExpired: FiniteDuration, queryBatchSize: Int)
