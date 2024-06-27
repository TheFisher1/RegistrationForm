package users

import cats.data.{Chain, Validated}
import cats.data.Validated.{Invalid, Valid}
import doobie.free.connection.ConnectionIO
import doobie.util.transactor.Transactor
import org.scalamock.clazz.Mock
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.shouldBe
import users.validation.PasswordError.{PasswordRequiresGreaterVariety, PasswordTooShort}
import users.validation.{PasswordError, PasswordErrors, RegistrationForm, RegistrationFormError, RegistrationFormErrors, Validator}
import utils.PasswordUtils

class ValidationTest extends AnyFlatSpec with MockFactory:
//  val transactor: Transactor[ConnectionIO] = mock[Transactor[ConnectionIO]]
  
  "An Empty Form " should " generate errors" in {
    val form = RegistrationForm("", "", "", "")
    RegistrationApp.registerUser(form) shouldBe
      Invalid(RegistrationFormErrors(Chain(

      RegistrationFormError.InvalidUsername, RegistrationFormError.InvalidPassword(PasswordErrors(Chain(PasswordError.PasswordTooShort, PasswordError.PasswordRequiresGreaterVariety))),
      RegistrationFormError.InvalidEmail)))
  }

  "password" should " be correctly validated" in {
    val passwordTooShort = "123@kK"
    Validator.validatePassword(passwordTooShort) shouldBe Invalid(RegistrationFormErrors(RegistrationFormError.InvalidPassword(PasswordErrors(PasswordTooShort))))

    val passwordRequiresGreaterVariety = "password123"

    Validator.validatePassword(passwordRequiresGreaterVariety) shouldBe Invalid(RegistrationFormErrors(RegistrationFormError.InvalidPassword(PasswordErrors(PasswordRequiresGreaterVariety))))
  }

  "password" should " be correctly processed when it is correct" in {
    val correctPassword = "p@ssW0rd123"

    Validator.validatePassword(correctPassword) match
      case Valid(x) => succeed
      case Invalid(x) => fail()
  }

  "password " should " match its confirmation" in {
    val password = "p@ssW0rd123"
    val conf = "p@ssW0rd123"
    
//    val form = RegistrationForm(null, null, password, conf)
    
//    RegistrationApp.processUser(transactor)
    
    
//    ConnectionIO[String] = Return(foo)

  }
  



