package zio2.https2.example.api

import caliban.{CalibanError, GraphQLInterpreter, Http4sAdapter}
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import org.http4s.server.middleware.CORS
import zio.ZIO
import zio.interop.catz.*
import zio2.https2.example.Types.{AppTask, Requirements}
import zio2.https2.example.api.HealthCheckApi
import zio2.https2.example.config.HttpConfig

object ServerHttp4s:
  val run: AppTask[Nothing] =
    for
      httpConfig <- ZIO.service[HttpConfig]
      graphqlInterpreter <- ProductGraphqlSchema.api.interpreter
      server <- runServer(httpConfig, graphqlInterpreter)
    yield server

  private def runServer(
                         httpConfig: HttpConfig,
                         graphQLInterpreter: GraphQLInterpreter[Requirements, CalibanError]
                       ): AppTask[Nothing] =
    BlazeServerBuilder[AppTask].withoutBanner
      .bindHttp(httpConfig.port, httpConfig.host)
      .withHttpApp(
        CORS
          .policy(
            Router[AppTask](
              "/health" -> HealthCheckApi.routes,
              "/graphql" -> Http4sAdapter.makeHttpService(graphQLInterpreter)
            ).orNotFound
          )
      )
      .resource
      .useForever
  end runServer
end ServerHttp4s
