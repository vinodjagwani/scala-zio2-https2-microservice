package zio2.https2.example.service

import doobie.implicits.*
import doobie.util.transactor.Transactor
import zio.*
import zio.interop.catz.*
import zio.query.ZQuery
import zio2.https2.example.db.repository.ProductRepository
import zio2.https2.example.db.repository.model.Product
import zio2.https2.example.errors.AppError

class ProductQueryService(transactor: Transactor[Task]) {
  private val repo = ProductRepository()

  def getById(id: Long): IO[AppError, Option[Product]] =
    repo.findById(id).transact(transactor).mapError(AppError.db)

  def getAll(offset: Int, limit: Int): IO[AppError, List[Product]] =
    repo.findAll(limit).transact(transactor).mapError(AppError.db)
}

object ProductQueryService {
  val live: ZLayer[Transactor[Task], Nothing, ProductQueryService] = ZLayer.fromZIO {
    for {
      transactor <- ZIO.service[Transactor[Task]]
    } yield new ProductQueryService(transactor)
  }

  def getById(id: Long): ZQuery[ProductQueryService, AppError, Option[Product]] =
    ZQuery.fromZIO(ZIO.serviceWithZIO[ProductQueryService](_.getById(id)))

  def getAll(offset: Int, limit: Int): ZQuery[ProductQueryService, AppError, List[Product]] =
    ZQuery.fromZIO(ZIO.serviceWithZIO[ProductQueryService](_.getAll(offset, limit)))
}
