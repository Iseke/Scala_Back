package Algebra

sealed trait Calcul

case class Fail(res: String) extends Calcul
case class Success(res: Int) extends Calcul
//sum type pattern
