package zio2.https2.example.service

import doobie.implicits.*
import doobie.util.transactor.Transactor
import zio.*
import zio.interop.catz.*
import zio2.https2.example.db.repository.ProductRepository
import zio2.https2.example.errors.AppError

class ProductCommandService(transactor: Transactor[Task]) {
  private val repo = ProductRepository()

  def delete(id: Long): IO[AppError, Boolean] =
    repo.delete(id).transact(transactor).map(_ > 0).mapError(AppError.db)
}

object ProductCommandService {
  val live: ZLayer[Transactor[Task], Nothing, ProductCommandService] = ZLayer.fromZIO {
    for {
      transactor <- ZIO.service[Transactor[Task]]
    } yield new ProductCommandService(transactor)
  }

  def delete(id: Long): ZIO[ProductCommandService, AppError, Boolean] =
    ZIO.serviceWithZIO[ProductCommandService](_.delete(id))
      .retry(
        Schedule.exponential(500.millis) &&
          Schedule.recurs(3)
            .whileInput {
              case _: AppError.DbLevelError => true
              case _ => false
            }
      )
      .timeoutFail(AppError.db("Delete operation timed out"))(5.seconds)
      .tapError {
        case dbError: AppError.DbLevelError =>
          ZIO.logError(s"Database error deleting product $id: ${dbError.msg}")
        case validationError: AppError.ValidationError =>
          ZIO.logWarning(s"Validation failed deleting product $id: ${validationError.msg}")
        case timeout: AppError =>  
          ZIO.logWarning(s"Timeout deleting product $id: ${timeout.toThrowable}")
      }.orElseSucceed(false)
}