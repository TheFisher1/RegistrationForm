package users

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import dal.DAO
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import doobie.Transactor
import doobie.implicits.toSqlInterpolator
import doobie.implicits.toConnectionIOOps
import org.postgresql.util.PSQLException

class DAOTest extends AsyncFlatSpec with BeforeAndAfterAll with BeforeAndAfterEach with Matchers with AsyncIOSpec:
  val transactor: Transactor[IO] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql://localhost:5432/users?serverTimezone=UTC",
    "kaloyanribarov",
    "password123"
  )

  def createTable: doobie.ConnectionIO[Int] =
    sql"""CREATE TABLE test_users (
          |username VARCHAR NOT NULL UNIQUE,
          |password VARCHAR NOT NULL,
          |email VARCHAR NOT NULL UNIQUE
          |)
          |""".stripMargin.update.run

  def delete: doobie.ConnectionIO[Int] =
    sql"DELETE FROM test_users WHERE email = 'email3'".update.run
  val dao: DAO[IO] = DAO(transactor, "test_users")

  val user: User = User("username1", "password1", "email1")
  val insertElem: doobie.ConnectionIO[Int] =
    sql"INSERT INTO test_users VALUES(${user.username}, ${user.password}, ${user.email})"
      .update
      .run

  override def beforeAll(): Unit =
//    createTable.transact(transactor)
      IO.unit

  "DAO" should " correctly add users" in {
    dao.insert(User("user3", "password3", "email3")).asserting (_ shouldBe 1)
  }

  it should "throw exception when db throws" in {

    assertThrows[PSQLException] {
      dao.insert(user).unsafeRunSync()
    }
  }

  it should "correctly find users according to an email" in {
    dao.selectUser("email1").asserting(_ shouldBe a[Some[_]])
  }

  it should "not throw exception when db does not find user" in {
    dao.selectUser("email4").assertNoException
  }

  it should " change credentials" in {
    dao.changeEmail("newEmail1", "email1").unsafeRunSync()
    dao.selectUser("newEmail1").asserting(_ shouldBe Some(
      user.copy(email = "newEmail1"))
    )

    dao.changePassword("newPassword1", "newEmail1").unsafeRunSync()
    dao.selectUser("newEmail1").asserting(_ shouldBe Some(user.copy(password = "newPassword1", email = "newEmail1")))

    dao.changeUsername("newUsername1", "newEmail1").unsafeRunSync()
    dao.selectUser("newEmail1").asserting(_ shouldBe Some(User("newUsername1", "newPassword1", "newEmail1")))
  }



  override def afterAll(): Unit =
    dao.changeEmail("email1", "newEmail1").unsafeRunSync()
    dao.changePassword("password1", "email1").unsafeRunSync()
    dao.changeUsername("username1", "newUsername1").unsafeRunSync()
    delete.transact(transactor).unsafeRunSync()


