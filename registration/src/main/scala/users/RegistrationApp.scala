package users

import doobie.implicits.toConnectionIOOps
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.effect.IO
import users.validation.RegistrationFormErrors
import cats.effect.unsafe.implicits.global
import cats.implicits.catsSyntaxFlatMapOps
import sql.{DBMS, transactor}
import users.validation.{RegistrationForm, Validator}

object RegistrationApp:

//  def insertQuery(user: User) =
//    sql"""INSERT INTO users VALUES ${user.username}, ${user.email}, ${user.password}})"""
  
  def registerUser: IO[Unit] =
    for {
      form <- createRegistrationForm
      validatedUser = Validator.validate(form)
      _ <- produceOutput(validatedUser)
      _ <- IO.println("Would yoy like to continue? -- y | n")
      _ <- askForContinuance
  } yield ()

  private def addUser(user: User): IO[Unit] =
    DBMS.insert(user).transact(transactor).attempt.flatMap {
      case Left(value) => IO.println("Email or Username is already in use")
      case Right(success) => IO.println("Uses was successfully added")
    }


  private def produceOutput(validated: Validated[RegistrationFormErrors, User]): IO[Unit] =
    validated match
      case Valid(user) =>
        addUser(user)
      case Invalid(errors) => mapErrors(errors)
  
  private def mapErrors(errors: RegistrationFormErrors): IO[Unit] =
    IO.println("there were errors. ") >> IO.println(errors.errors)
    
  
  private def createRegistrationForm: IO[RegistrationForm] =
    for {
      username <- promptForInput("Please enter your username: ")

      password <- promptForInput("Please enter your password")

      passwordConfirmation <- promptForInput("Please confirm your password: ")

      email <- promptForInput("Please enter email: ")
      
    } yield RegistrationForm(username, email, password, passwordConfirmation)
  
  private def askForContinuance: IO[Unit] =
    val shouldContinue = IO.readLine
    shouldContinue.flatMap { answer =>
      val yesOrNo = answer == "y" || answer == "yes"
      continue(yesOrNo)
    }
    
  private def promptForInput(prompt: String): IO[String] =
    IO.println(prompt) >>
    IO.readLine
  
  private def continue(yes: Boolean) = 
    if yes then registerUser else IO.println("successfully exited!")

@main def h(): Unit = RegistrationApp.registerUser.unsafeRunSync()


