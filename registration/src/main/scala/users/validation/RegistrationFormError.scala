package users.validation

import cats.data.*
import cats.data.Validated.{Invalid, Valid}
import cats.kernel.Semigroup

enum RegistrationFormError:
  case InvalidUsername

  case InvalidEmail
  case PasswordsDoNotMatch
  case EmailIsInUse

  case InvalidPassword(errors: PasswordErrors)

enum PasswordError:
  case PasswordTooShort

  case PasswordRequiresGreaterVariety

case class RegistrationFormErrors(errors: Chain[RegistrationFormError])
//  override def toString: String =
//    errors.map(_.toString).foldLeft("")((acc, elem) => acc + elem)

object RegistrationFormErrors:
  def apply(registrationFormError: RegistrationFormError): RegistrationFormErrors =
    RegistrationFormErrors(Chain(registrationFormError))
    
  given Semigroup[RegistrationFormErrors] with
    override def combine(x: RegistrationFormErrors, y: RegistrationFormErrors): RegistrationFormErrors =
      RegistrationFormErrors(x.errors ++ y.errors)
  

case class PasswordErrors(errors: Chain[PasswordError]):
  override def toString: String = errors.toString
  
object PasswordErrors:
  def apply(passwordError: PasswordError): PasswordErrors =
    PasswordErrors(Chain(passwordError))
    
  given Semigroup[PasswordErrors] with
    override def combine(x: PasswordErrors, y: PasswordErrors): PasswordErrors = 
      PasswordErrors(x.errors ++ y.errors)
