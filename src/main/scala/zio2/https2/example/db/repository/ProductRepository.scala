package zio2.https2.example.db.repository

import doobie.*
import doobie.implicits.*
import zio2.https2.example.db.repository.model.Product

final class ProductRepository(schema: String = "public") {

  private val table = Fragment.const(s"$schema.product")

  def findById(id: Long): ConnectionIO[Option[Product]] = {
    (fr"SELECT id, name, description FROM" ++ table ++ fr"WHERE id = $id")
      .query[Product].option
  }

  def findAll(limit: Int = 100): ConnectionIO[List[Product]] = {
    (fr"SELECT id, name, description FROM" ++ table ++ fr"LIMIT $limit")
      .query[Product].to[List]
  }

  def delete(id: Long): ConnectionIO[Int] = {
    (fr"DELETE FROM" ++ table ++ fr"WHERE id = $id")
      .update.run
  }
}
