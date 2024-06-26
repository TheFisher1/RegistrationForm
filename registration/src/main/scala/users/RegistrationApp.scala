package users

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}

import cats.syntax.contravariantSemigroupal.catsSyntaxTuple3Semigroupal

import users.validation.RegistrationFormErrors
import users.validation.{RegistrationForm, Validator}

import users.validation.Validator.{passwordIsAcceptable, validateEmail, validateUsername}
import scala.io.StdIn

object RegistrationApp:
  
  def registerUser(registrationForm: RegistrationForm): Validated[RegistrationFormErrors, User] =
    (
      validateUsername(registrationForm),
      passwordIsAcceptable(registrationForm.password, registrationForm.passwordVerification),
      validateEmail(registrationForm)
    ).mapN(User.apply)

