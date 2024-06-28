package users

import cats.effect.kernel.Sync

import scala.util.Random

class Captcha(val randomString: String):
  def check(answer: String): Boolean =
    answer == randomString

object Captcha:
  val MIN_LENGTH = 5
  
  def apply(): Captcha =
    val randomString = ('a' until 'Z') ++ (1 to 100)
    val list = Random
      .shuffle(randomString.tails)
      .map(_.mkString)
      .filter(_.length > 5)
      .filter(_.length < 15)
      .toList
      new Captcha(list(Random.nextInt() % list.size))
