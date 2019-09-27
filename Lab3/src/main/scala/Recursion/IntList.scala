package Recursion


sealed trait IntList {
  def length(): Int = {
    def nestedMethod(node: IntList, n: Int): Int = node match {
      case Node(head, tail) => nestedMethod(tail, n + 1)
      case End => n
    }

    nestedMethod(this, 0)
  }

  def product(): Int = {
    def nestedMethod(node: IntList, n: Int): Int = node match {
      case Node(head, tail) => nestedMethod(tail, n * head)
      case End => n
    }

    nestedMethod(this, 1)
  }

  def double(): IntList = {
    def nestedMethod(node: IntList): IntList = node match {
      case Node(head, tail) => Node(2 * head, nestedMethod(tail))
      case End => End
    }

    nestedMethod(this)
  }

}

case object End extends IntList
case class Node(head: Int, tail: IntList) extends IntList

object Main extends App {
  val intList = Node(1, Node(2, Node(3, Node(4, End))))


  println(intList.length())
  println(intList.tail.length())
  println(End.length())// IntList length


  println(intList.product())
  println(intList.tail.product())
  println(End.product())// IntList product


  println(intList.double())
  println(intList.tail.double())
  println(End.double())// IntList double
}
