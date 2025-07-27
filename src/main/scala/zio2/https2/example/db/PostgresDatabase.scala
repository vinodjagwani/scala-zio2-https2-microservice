package zio2.https2.example.db

import com.zaxxer.hikari.HikariConfig
import doobie.hikari.HikariTransactor
import doobie.util.transactor.Transactor
import org.flywaydb.core.Flyway
import zio.*
import zio.interop.catz.*
import zio.interop.catz.implicits.*
import zio2.https2.example.config.DbConfig

import scala.concurrent.ExecutionContext

object PostgresDatabase:
  private def migrate(config: DbConfig): Task[Unit] =
    ZIO.attempt {
      Flyway
        .configure()
        .dataSource(config.jdbcUrl, config.user, config.password)
        .load()
        .migrate()
    }.unit
  end migrate

  private def makeTransactor(config: DbConfig, ec: ExecutionContext): RIO[Scope, Transactor[Task]] =
    val hikariConfig = HikariConfig()
    hikariConfig.setJdbcUrl(config.jdbcUrl + "?currentSchema=public")
    hikariConfig.setUsername(config.user)
    hikariConfig.setPassword(config.password)
    hikariConfig.addDataSourceProperty("loggerLevel", "TRACE")
    hikariConfig.setDriverClassName(config.driver)
    hikariConfig.setConnectionTimeout(config.connectionTimeout)
    hikariConfig.setMinimumIdle(config.minimumIdle)
    hikariConfig.setMaximumPoolSize(config.maximumPoolSize)

    HikariTransactor
      .fromHikariConfig[Task](hikariConfig, ec)
      .toScopedZIO
  end makeTransactor

  val transactorLive: URLayer[Scope & DbConfig, Transactor[Task]] =
    ZLayer
      .fromZIO(
        for
          _ <- ZIO.logDebug("Constructing layer PostgresDatabase")
          config <- ZIO.service[DbConfig]
          _ <- migrate(config)
          executionContext <- ZIO.descriptor
            .map(_.executor.asExecutionContext)
          transactor <- makeTransactor(config, executionContext)
        yield transactor
      )
      .tapError(throwable => ZIO.logError(throwable.getMessage))
      .orDie

end PostgresDatabase
