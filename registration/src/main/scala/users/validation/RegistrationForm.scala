package users.validation

import cats.data.Validated.{Invalid, Valid}
import cats.data.Validated
import users.validation.RegistrationFormError.{InvalidEmail, InvalidPassword, PasswordsDoNotMatch}
import utils.PasswordUtils
import cats.implicits.*

import scala.language.postfixOps

type Email = String
type Username = String

case class RegistrationForm(username: Username, email: Email, password: String, passwordVerification: String)

class Validator:
  val PASSWORD_MIN_LENGTH = 8

  def validateUsername(registrationForm: RegistrationForm): Validated[RegistrationFormErrors, Username] =
    val name = registrationForm.username
    
    name match
      case x if x.isEmpty || x.isBlank => Invalid(RegistrationFormErrors(RegistrationFormError.InvalidUsername))
      case x => Valid(name)

  def validateEmail(registrationForm: RegistrationForm): Validated[RegistrationFormErrors, Email] =
    val email = registrationForm.email
    if """(?=[^\s]+)(?=(\w+)@([\w.]+))""".r.findFirstIn(email).isEmpty then
      Invalid(RegistrationFormErrors(InvalidEmail))
    else
      Valid(email)

  
  def validatePassword(password: String): Validated[RegistrationFormErrors, String] =
    def tooShortPassword: Validated[PasswordErrors, String] =
      if password.length < 8 then Invalid(PasswordErrors(PasswordError.PasswordTooShort)) else Valid(password)

    def passwordRequiresGreaterSymbolVariety: Validated[PasswordErrors, String] =
      if !password.exists(_.isDigit)
        || !password.exists(_.isLetter)
        || password.forall(_.isLetterOrDigit) then Invalid(PasswordErrors(PasswordError.PasswordRequiresGreaterVariety))
      else Valid(password)
    
    (
      tooShortPassword,
      passwordRequiresGreaterSymbolVariety
    ).tupled
      .fold(
      passwordErrors => Invalid(RegistrationFormErrors(
        InvalidPassword(passwordErrors))
      ),
      (_, _) => Valid(
        PasswordUtils.hash(password)
      )
    )

  def passwordsMatch(password: String, confirmation: String): Validated[RegistrationFormErrors, String] =
    if password == confirmation then Valid(PasswordUtils.hash(password))
    else Invalid(RegistrationFormErrors(PasswordsDoNotMatch))

  def passwordIsAcceptable(password: String, passwordConfirmation: String): Validated[RegistrationFormErrors, String] =
    validatePassword(password).product(passwordsMatch(password, passwordConfirmation)).fold(
      i => Invalid(i),
      (p, _) => Valid(p)
    )
  
