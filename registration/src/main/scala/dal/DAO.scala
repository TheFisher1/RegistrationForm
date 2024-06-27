package dal

import cats.Monad
import cats.effect.kernel.Sync
import cats.effect.unsafe.implicits.global
import cats.effect.{IO, MonadCancelThrow, Resource}
import cats.implicits.catsSyntaxFlatMapOps
import doobie.hikari.HikariTransactor
import doobie.implicits.toSqlInterpolator
import doobie.syntax.connectionio.toConnectionIOOps
import doobie.*
import users.User
import users.validation.Validator

val transactor1: Transactor[IO] = Transactor.fromDriverManager[IO](
  "org.postgresql.Driver",
  "jdbc:postgresql://localhost:5432/users?serverTimezone=UTC",
  "kaloyanribarov",
  "password123"
)

class DAO[F[_] : Sync](transactor: Transactor[F]):
  val TABLE_USERS = "users_test"
    val createTable = sql"""
        CREATE TABLE users_test (
          username VARCHAR NOT NULL UNIQUE,
          password VARCHAR NOT NULL,
          email VARCHAR NOT NULL UNIQUE
        )
      """.update.run

  def insert(user: User): F[Int] =
    sql"INSERT INTO users_test VALUES (${user.username}, ${user.password}, ${user.email})"
      .update
      .run
      .transact(transactor)

  def selectAll: F[List[User]] =
    sql"SELECT * FROM users_test".query[User].to[List]
      .transact(transactor)

  def selectUser(email: String): F[Option[User]] =
    sql"SELECT * FROM users_test WHERE email = $email"
      .query[User]
      .option
      .transact(transactor)

  def changeUsername(newUsername: String, email: String): F[Int] =
    sql"UPDATE users_test SET username = $newUsername WHERE email = $email"
      .update
      .run
      .transact(transactor)

  def changeEmail(newEmail: String, email: String): F[Int] =
    sql"UPDATE users_test SET email = $newEmail WHERE email = $email"
      .update
      .run
      .transact(transactor)

  def changePassword(newPassword: String, email: String): F[Int] =
    sql"UPDATE users_test SET password = $newPassword WHERE email = $email"
      .update
      .run
      .transact(transactor)

//  def changeUsername
//  @main def h =
//    val a = insert(User("username6", "password6", "email6"))
//
//    val b = sql"SELECT * FROM users_test".query[User].to[List]
//
//    val d =  a.transact(transactor).handleErrorWith { e
//       => IO.println("there was error")
//    } >> b.transact(transactor) >>= IO.println
//    d.unsafeRunSync()



