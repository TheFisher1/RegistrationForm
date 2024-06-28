package dal

import cats.effect.kernel.Sync

import doobie.implicits.toSqlInterpolator
import doobie.syntax.connectionio.toConnectionIOOps
import doobie.*
import users.User

class DAO[F[_] : Sync](transactor: Transactor[F], table: String):
  private val createTable = sql"""CREATE TABLE $table (
        username VARCHAR NOT NULL UNIQUE,
        password VARCHAR NOT NULL,
        email VARCHAR NOT NULL UNIQUE
    )
    """.update.run

  def create: F[Int] =
    createTable.transact(transactor)

  def insert(user: User): F[Int] =
    sql"INSERT INTO ${Fragment.const(table)} VALUES (${user.username}, ${user.password}, ${user.email})"
      .update
      .run
      .transact(transactor)

  def selectUser(email: String): F[Option[User]] =
    sql"SELECT * FROM ${Fragment.const(table)} WHERE email = $email"
      .query[User]
      .option
      .transact(transactor)

  def changeUsername(newUsername: String, email: String): F[Int] =
    sql"UPDATE ${Fragment.const(table)} SET username = $newUsername WHERE email = $email"
      .update
      .run
      .transact(transactor)

  def changeEmail(newEmail: String, email: String): F[Int] =
    sql"UPDATE ${Fragment.const(table)} SET email = $newEmail WHERE email = $email"
      .update
      .run
      .transact(transactor)

  def changePassword(newPassword: String, email: String): F[Int] =
    sql"UPDATE ${Fragment.const(table)} SET password = $newPassword WHERE email = $email"
      .update
      .run
      .transact(transactor)



