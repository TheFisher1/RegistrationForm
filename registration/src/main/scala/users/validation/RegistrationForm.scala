package users.validation

import cats.data.Validated.{Invalid, Valid}
import cats.data.{Chain, NonEmptyChain, Validated}
import cats.syntax.all.catsSyntaxTuple3Semigroupal
import users.User
import users.validation.PasswordError.PasswordsDoNotMatch
import users.validation.RegistrationFormError.{InvalidEmail, InvalidPassword}
import utils.PasswordUtils
import cats.implicits._

import scala.language.postfixOps

type Email = String
type Username = String

case class RegistrationForm(username: Username, email: Email, password: String, passwordVerification: String)

object Validator:
  val PASSWORD_MIN_LENGTH = 8

  private def validateUsername(registrationForm: RegistrationForm): Validated[RegistrationFormErrors, Username] =
    val name = registrationForm.username

    name match
      case x if x.isEmpty || x.isBlank => Invalid(RegistrationFormErrors(RegistrationFormError.InvalidUsername))
      case x => Valid(name)

  private def validateEmail(registrationForm: RegistrationForm): Validated[RegistrationFormErrors, Email] =
    val email = registrationForm.email
    if """(?=[^\s]+)(?=(\w+)@([\w.]+))""".r.findFirstIn(email).isEmpty then
      Invalid(RegistrationFormErrors(InvalidEmail))
    else
      Valid(email)

  private def validatePassword(registrationForm: RegistrationForm): Validated[RegistrationFormErrors, String] =
    val password = registrationForm.password

    def tooShortPassword: Validated[PasswordErrors, String] =
      if password.length < 8 then Invalid(PasswordErrors(PasswordError.PasswordTooShort)) else Valid(password)

    def passwordRequiresGreaterSymbolVariety: Validated[PasswordErrors, String] =
      if !password.exists(_.isDigit)
        || !password.exists(_.isLetter)
        || password.forall(_.isLetterOrDigit) then Invalid(PasswordErrors(PasswordError.PasswordRequiresGreaterVariety))
      else Valid(password)

    def passwordsDoNotMatch: Validated[PasswordErrors, String] =
      if registrationForm.passwordVerification != password then
        Invalid(PasswordErrors(PasswordsDoNotMatch)) else Valid(password)
    
    (
      tooShortPassword,
      passwordRequiresGreaterSymbolVariety,
      passwordsDoNotMatch
    ).tupled
      .fold(
        passwordErrors => Invalid(RegistrationFormErrors(
        InvalidPassword
          ( passwordErrors))),
        (_, _, _) => Valid(password))
    
    
  def validate(registrationForm: RegistrationForm): Validated[RegistrationFormErrors, User] =
    (
      validateUsername(registrationForm),
      validatePassword(registrationForm),
      validateEmail(registrationForm)
    ).mapN(User.apply)
  
