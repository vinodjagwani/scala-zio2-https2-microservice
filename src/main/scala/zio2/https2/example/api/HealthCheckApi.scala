package zio2.https2.example.api

import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.HttpRoutes
import org.http4s.circe.*
import org.http4s.dsl.Http4sDsl
import zio.*
import zio.interop.catz.*
import zio2.https2.example.service.HealthCheckService
import zio2.https2.example.utils.ZIOUtils.*

object HealthCheckApi {
  def routes[R <: HealthCheckService]: HttpRoutes[RIO[R, *]] = {
    type T[A] = RIO[R, A]
    val dsl = Http4sDsl[T]
    import dsl.*

    HttpRoutes.of[T] { case GET -> Root =>
      for {
        status <- HealthCheckService.status.errorAsThrowable
        response <- if (status.isOk) Ok(status.asJson)
        else InternalServerError(status.asJson)
      } yield response
    }
  }
}