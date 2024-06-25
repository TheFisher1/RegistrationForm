package users

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.effect.IO
import users.validation.RegistrationFormErrors
import cats.effect.unsafe.implicits.global
import cats.implicits.catsSyntaxFlatMapOps
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
  IO.println(s"successfully added user with username ${user.username}")
  
  
  private def produceOutput(validated: Validated[RegistrationFormErrors, User]): IO[Unit] =
    validated match
      case Valid(user) => 
        /* adding the user */ addUser(user)
      case Invalid(errors) => IO.println("there were errors: ") >> IO(errors.toString)
  
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
      val yes = answer == "y" || answer == "yes"
      continue(yes)
    }
    
  private def promptForInput(prompt: String): IO[String] =
    IO.println(prompt) >>
    IO.readLine
  
  private def continue(yes: Boolean) = 
    if yes then registerUser else IO.println("successfully exited!")

@main def h(): Unit = RegistrationApp.registerUser.unsafeRunSync()


