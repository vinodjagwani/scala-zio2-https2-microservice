package zio2.https2.example.api

import caliban.CalibanError.ExecutionError
import caliban.GraphQL.graphQL
import caliban.schema.ArgBuilder.*
import caliban.schema.{ArgBuilder, GenericSchema, Schema}
import caliban.{CalibanError, GraphQL, RootResolver}
import zio.ZIO
import zio.query.ZQuery
import zio2.https2.example.Types.Requirements
import zio2.https2.example.db.repository.model.Product
import zio2.https2.example.service.{ProductCommandService, ProductQueryService}
import zio2.https2.example.utils.ZIOUtils.*

object ProductGraphqlSchema extends GenericSchema[Requirements]:

  private type CalibanIO[R, A] = ZIO[R, CalibanError, A]
  private type CalibanQuery[R, A] = ZQuery[R, CalibanError, A]

  private case class IdArgs(id: Long)

  private case class PaginationArgs(offset: Option[Int], limit: Option[Int])

  private case class Queries(
                              productById: IdArgs => CalibanQuery[ProductQueryService, Option[Product]],
                              allProducts: PaginationArgs => CalibanQuery[ProductQueryService, List[Product]]
                            )

  private case class Mutations(deleteProduct: IdArgs => CalibanIO[ProductCommandService, Boolean])

  private val queries = Queries(
    args => ProductQueryService.getById(args.id).errorAsCaliban,
    args => ProductQueryService.getAll(args.offset.getOrElse(0), args.limit.getOrElse(100)).errorAsCaliban
  )

  private val mutations = Mutations(
    args => ProductCommandService.delete(args.id).errorAsCaliban
  )

  val api: GraphQL[Requirements] = graphQL[Requirements, Queries, Mutations, Unit](RootResolver(queries, mutations))

  given Schema[Any, Product] = Schema.gen

end ProductGraphqlSchema
