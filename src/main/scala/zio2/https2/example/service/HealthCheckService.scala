package zio2.https2.example.service

import doobie.*
import doobie.implicits.*
import io.circe.Encoder
import zio.*
import zio.interop.catz.*
import zio2.https2.example.config.{HealthCheckConfig, HttpConfig}
import zio2.https2.example.errors.AppError

import scala.language.postfixOps

class HealthCheckService(healthCheckConfig: HealthCheckConfig, transactor: Transactor[Task]):

  private def status: IO[AppError, HealthCheckStatusSummary] =
    for postgres <-
          fr"SELECT 1"
            .query[Int]
            .option
            .transact(transactor)
            .timeoutTo(HealthCheckStatus.Timeout)(identity)(
              healthCheckConfig.postgresTimeoutSeconds seconds
            )
            .map {
              case Some(1) => HealthCheckStatus.Ok
              case _ => HealthCheckStatus.NotOk
            }
            .mapError(AppError.db)
    yield HealthCheckStatusSummary(postgres)

object HealthCheckService:
  def status: ZIO[HealthCheckService, AppError, HealthCheckStatusSummary] =
    ZIO.serviceWithZIO[HealthCheckService](_.status)

  val live: ZLayer[Transactor[Task] & HttpConfig, Nothing, HealthCheckService] = ZLayer.fromZIO {
    for
      _ <- ZIO.logDebug("Constructing HealthCheck layer")
      httpConfig <- ZIO.service[HttpConfig]
      transactor <- ZIO.service[Transactor[Task]]
    yield HealthCheckService(httpConfig.healthCheckConfig, transactor)
  }

case class HealthCheckStatusSummary(postgres: HealthCheckStatus):
  def isOk: Boolean = postgres == HealthCheckStatus.Ok

enum HealthCheckStatus:
  case Ok, NotOk, Timeout

object HealthCheckStatus:
  given encoder: Encoder[HealthCheckStatus] =
    Encoder.encodeString.contramap(_.toString)
end HealthCheckStatus
