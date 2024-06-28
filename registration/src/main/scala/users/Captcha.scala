package users

import cats.effect.kernel.Sync
import cats.effect.std.SecureRandom

import scala.util.Random

class Captcha(val randomString: String):
  def check(answer: String): Boolean =
    answer == randomString

object Captcha:
  val MIN_LENGTH = 5
  
  def apply(): Captcha =
    val allowedCharactersForGeneratingTheString = ('a' until 'Z') ++ (1 to 10)
    val list = Random
      .shuffle(allowedCharactersForGeneratingTheString)
      .combinations(10)
      .map(_.mkString)
      .toList
      new Captcha(list(Random.nextInt() % list.size))
