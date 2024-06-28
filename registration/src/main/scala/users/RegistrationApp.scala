package users

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}

import cats.syntax.contravariantSemigroupal.catsSyntaxTuple3Semigroupal

import users.validation.RegistrationFormErrors
import users.validation.{RegistrationForm, Validator}

object RegistrationApp:
  
  val validator: Validator = Validator()
  
  def registerUser(registrationForm: RegistrationForm): Validated[RegistrationFormErrors, User] =
    (
      validator.validateUsername(registrationForm),
      validator.passwordIsAcceptable(registrationForm.password, registrationForm.passwordVerification),
      validator.validateEmail(registrationForm)
    ).mapN(User.apply)

