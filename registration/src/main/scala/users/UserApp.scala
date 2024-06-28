package users

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.effect
import cats.effect.{IO, kernel}
import cats.effect.kernel.Sync
import cats.effect.unsafe.implicits.global
import dal.DAO
import doobie.Transactor
import users.validation.{RegistrationForm, RegistrationFormErrors, Validator}
import utils.PasswordUtils
import cats.syntax.flatMap.toFlatMapOps
import cats.syntax.functor.toFunctorOps
import cats.syntax.flatMap.catsSyntaxFlatMapOps
import cats.syntax.applicativeError.catsSyntaxApplicativeError

import scala.io.StdIn
import scala.language.postfixOps

val transactor: Transactor[IO] = Transactor.fromDriverManager[IO](
  "org.postgresql.Driver",
  "jdbc:postgresql://localhost:5432/users?serverTimezone=UTC",
  "kaloyanribarov",
  "password123"
)

class UserApp[F[_] : Sync](dao: DAO[F], validator: Validator):
  private def userPasswordMatches(user: Option[User], password: String): F[Boolean] =
    Sync[F].pure(user.exists(u => PasswordUtils.check(password, u.password)))

  private def transformIntoOutput(flag: Boolean): F[Unit] =
    if flag then Sync[F].pure(println ("log-in was successful"))
    else Sync[F].pure(println ("Username or password were not correct, please try again!"))

  def changeCredentials(input: String, email: String): F[Unit] = input match
    case "username" => changeUsername(email)
    case "password" => changePassword(email)
    case "email" => changeEmail(email)
    case "no" | "n" => doYouWantToStayLoggedIn(email)
    case _ => Sync[F].pure(println ("Please provide a valid option")) >> doYouWantToStayLoggedIn(email)

  private def doYouWantToStayLoggedIn(email: String): F[Unit] =
    val shouldBePrinted = true
    Sync[F].pure(println(" Do you want to stay logged in? ")) >>
      Sync[F].pure(StdIn.readLine()).flatMap {
        case "n" | "no" => enterProgram
        case _ => changeInit(true, shouldBePrinted)(email)
      }

  private def changeEmail(email: String): F[Unit] =
    for {
    _ <- Sync[F].pure(println("What would you like to be your new email? "))
    newEmail <- Sync[F].delay(StdIn.readLine())

    _ <- dao.changeEmail(newEmail, email)

    _ <- Sync[F].pure(println("Your email was successfully changed."))

    _ <- doYouWantToStayLoggedIn(newEmail)
  } yield ()


  private def changeUsername(email: String): F[Unit] =
    for {
      _ <- Sync[F].pure(println ("What would you like to be your new username? "))
      newUsername <- Sync[F].pure(StdIn.readLine())

      _ <- dao.changeUsername(newUsername, email)

      _ <- Sync[F].pure(println("The username was successfully changed."))
      _ <- doYouWantToStayLoggedIn(email)
    } yield ()

  private def changePassword(email: String): F[Unit] =

    def validate(password: String): F[Unit] =
      validator.validatePassword(password) match
        case Valid(p) =>
          dao.changePassword(p, email)
            >> Sync[F].pure(println ("Successfully changed password"))
            >> doYouWantToStayLoggedIn(email)

        case Invalid(e) => Sync[F].pure(println(e.toString))

    for {
      _ <- Sync[F].pure(println("What would you like to be your new password? "))
      newPassword <- Sync[F].pure(StdIn.readLine())
      _ <- validate(newPassword)
    } yield ()

  private def logInUtil: F[Unit] =
    val print = true
    for {
      _ <- Sync[F].pure(println("Please enter your email: "))
      email <- Sync[F].pure(StdIn.readLine)

      _ <- Sync[F].pure(println("Please enter password: "))
      password <- Sync[F].pure(StdIn.readLine)

      user <- dao.selectUser(email)
      isPasswordCorrect <- userPasswordMatches(user, password)

      _ <- changeInit(isPasswordCorrect, print)(email)
    } yield ()

  private def changeInit(isPasswordCorrect: Boolean, shouldBePrinted: Boolean)(email: String): F[Unit] =
    for {
      _ <- transformIntoOutput(isPasswordCorrect).ensuring(shouldBePrinted)
      _ <- if isPasswordCorrect then for {
          _ <- Sync[F].pure(println("Do you want to change your username | password | email ?"))
          choice <- Sync[F].delay(StdIn.readLine())
          _ <- changeCredentials(choice, email)
        } yield ()

      else Sync[F].pure("")

    } yield ()

  def processUser: F[Unit] =
    for {
      form <- createRegistrationForm
      user = RegistrationApp.registerUser(form)
      _ <- produceOutput(user)
        .handleErrorWith(_ => Sync[F].pure(println("There was an error. ")))
    } yield ()

  private def register: F[Unit] = processUser

  private def logIn: F[Unit] = logInUtil

  private def continue(flag: Boolean): F[Unit] =
    if flag then enterProgram else Sync[F].delay(println:
                                          "Have a great day!")

  private def subscribe(choice: String): F[Unit] = choice match
    case "register" => register
    case "log-in" => logIn
    case _ => enterProgram

  private def addUser(user: User): F[Unit] =
    dao.insert(user).attempt
      .flatMap {
      case Left(value) => Sync[F].pure(println("Email or Username is already in use"))
      case Right(success) => Sync[F].pure(println("Uses was successfully added"))
    }

  private def produceOutput(validated: Validated[RegistrationFormErrors, User]): F[Unit] =
    validated match
      case Valid(user) =>
        addUser(user)
      case Invalid(errors) => mapErrors(errors)

  private def mapErrors(errors: RegistrationFormErrors): F[Unit] =
    Sync[F].pure(println("there were errors. ")) >> Sync[F].pure(println(errors.errors.toString))

  def createRegistrationForm: F[RegistrationForm] =
    for {
      username <- promptForInput("Please enter your username: ")
      password <- promptForInput("Please enter your password")
      passwordConfirmation <- promptForInput("Please confirm your password: ")
      email <- promptForInput("Please enter email: ")
      _ <- captchaLoop()
    } yield RegistrationForm(username, email, password, passwordConfirmation)

  private def enterCaptcha(): F[Boolean] =
    for {
      _ <- Sync[F].pure(println("I am not a robot: "))
      captcha = Captcha()
      _ <- Sync[F].pure(println(captcha.randomString))
      string <- Sync[F].pure(StdIn.readLine())

    } yield captcha.check(string)

  private def captchaLoop(): F[Unit] =
    enterCaptcha().map { b =>
      if b then Sync[F].pure(println("continue..."))
      else createRegistrationForm.void
    }

  private def promptForInput(prompt: String): F[String] =
    Sync[F].pure(println(prompt)) >> Sync[F].pure(StdIn.readLine)

  def enterProgram: F[Unit] =
    for {
      _ <- Sync[F].pure(println(
        """Please choose:
          |register a new user
          |log-in
          |change credentials
          |(if you opt for this option,
          |you will have to log-in first)"""))

      choice <- Sync[F].pure(StdIn.readLine)

      _ <- subscribe(choice)

      _ <- Sync[F].pure(println("Would you like to continue? -- y | n"))

      shouldContinue <- Sync[F].pure(StdIn.readLine)
      flag = shouldContinue != "n" && shouldContinue != "no"
      _ <- continue(flag)

    } yield ()

  def start: F[Unit] =
    for {
    _ <- Sync[F].pure(println ("Welcome to our app! What would you like to do?"))
    _ <- enterProgram
  } yield ()

@main def run(): Unit =
  val table = "users_test"
  val dao = DAO(transactor, table)
  UserApp(dao, Validator()).start.unsafeRunSync()

