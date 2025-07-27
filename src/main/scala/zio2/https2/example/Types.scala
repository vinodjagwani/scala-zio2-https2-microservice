package zio2.https2.example

import doobie.util.transactor.Transactor
import zio.{RIO, Task}
import zio2.https2.example.config.{AppConfig, DbConfig, HttpConfig}
import zio2.https2.example.service.{HealthCheckService, ProductCommandService, ProductQueryService}

object Types:
  type Requirements = HttpConfig & DbConfig & AppConfig & Transactor[Task] & HealthCheckService & ProductCommandService & ProductQueryService
  type AppTask[T] = RIO[Requirements, T]
end Types