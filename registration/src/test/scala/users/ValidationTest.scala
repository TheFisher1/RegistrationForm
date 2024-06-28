package users

import cats.data.{Chain, Validated}
import cats.data.Validated.{Invalid, Valid}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import users.validation.PasswordError.{PasswordRequiresGreaterVariety, PasswordTooShort}
import users.validation.{PasswordError, PasswordErrors, RegistrationForm, RegistrationFormError, RegistrationFormErrors, Validator}

class ValidationTest extends AnyFlatSpec with Matchers:
  val CORRECT_USERNAME = "ivan ivanov"
  val CORRECT_EMAIL = "ivanivanov@gmail.com"
  val VALIDATOR: Validator = Validator()
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
    VALIDATOR.validatePassword(passwordTooShort) shouldBe Invalid(RegistrationFormErrors(RegistrationFormError.InvalidPassword(PasswordErrors(PasswordTooShort))))

    val passwordRequiresGreaterVariety = "password123"

    VALIDATOR.validatePassword(passwordRequiresGreaterVariety) shouldBe Invalid(RegistrationFormErrors(RegistrationFormError.InvalidPassword(PasswordErrors(PasswordRequiresGreaterVariety))))
  }

  "password" should " be correctly processed when it is correct" in {
    val correctPassword = "p@ssW0rd123"

    VALIDATOR.validatePassword(correctPassword) match
      case Valid(x) => succeed
      case Invalid(x) => fail()
  }

  "password " should " match its confirmation" in {
    val password = "p@ssW0rd123"
    val conf = "p@ssW0rd123"

    val notMatchingConf = "p@ssW0rd12"

    val rightForm = RegistrationForm(CORRECT_USERNAME, CORRECT_EMAIL, password, conf)
    RegistrationApp.registerUser(rightForm) shouldBe a[Valid[User]]

    val leftForm = RegistrationForm(CORRECT_USERNAME, CORRECT_EMAIL, password, notMatchingConf)
    RegistrationApp.registerUser(leftForm) shouldBe a[Invalid[RegistrationFormErrors]]
  }


  



