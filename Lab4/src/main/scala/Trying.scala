object Trying extends App{

  val numbers: Seq[Int] = Seq(1,2,3,4,5)
  val nums =  numbers:+2 :+7 :+77 //append
  println(nums)

  val nums1 = 10+: numbers //prepend
  println(nums1)

  //filter

  println(nums.filter(x => x%2==0))
  val isEven = (x:Int) => x%2==0

  println(nums.filter(isEven))

  //sortWith

  println(numbers.sortWith((a,b) => a>b))
  println(numbers.sortWith((a,b)=>a<b))


  //find

  val find1: Option[Int] = numbers.find(x=>x==7)
  find1 match {
    case Some(value) => println(s"Found :${value}")
    case None => println("Not Found")
  }

  //contains

  println(numbers.contains(3))
  println(numbers.nonEmpty)

  println(numbers.exists(x=> x == 4 || x==3))


  numbers.foreach(x => println(x))
  println("////////////////////")
  numbers.foreach(println(_))
  numbers.foreach(println)

  println("//////////////")
  println(numbers)
  println(numbers.map(x => x * 2 + 5))



}
