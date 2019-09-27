object FirstPart extends App {
//  println("Hello world!!!")
  Draw(Circle(3))
  Draw(Rectangle(3,5))
  Draw(Square(4))
  println(Circle(2).getArea())
}
sealed trait Shape{
  def getSides(): Int
  def getPerm(): Double
  def getArea(): Double
}
trait Rectangular extends Shape{
  override def getSides(): Int = 4
}

case class Rectangle(a: Double, b: Double) extends  Rectangular{
  override def getPerm(): Double = 2*(a+b)
  override def getArea(): Double = a*b
}
case class Square(a: Double) extends Rectangular{
  override def getPerm(): Double = a*4
  override def getArea(): Double = math.pow(a,2)
}
case class Circle(r: Double) extends Shape{
  override def getPerm(): Double = 2*math.Pi*r
  override def getArea(): Double = math.Pi*r*r

  override def getSides(): Int = 0
}

object Draw {
  def apply(shape: Shape): Unit={
    shape match {
      case Rectangle(a,b) => println(s"A rectangle of width ${a}cm and height ${b}cm")
      case Square(a) => println(s"A square of side ${a}cm")
      case Circle(r) => println(s"A circle of radius ${r}cm" )
    }
  }
}