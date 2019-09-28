case class Film( name: String,
                 yearOfRelease: Int,
                 imdbRating: Double)
case class Director( firstName: String,
                     lastName: String,
                     yearOfBirth: Int,
                     films: Seq[Film])


object Movie extends App{
  val memento = new Film("Memento", 2000, 8.5)
  val darkKnight = new Film("Dark Knight", 2008, 9.0)
  val inception = new Film("Inception", 2010, 8.8)
  val highPlainsDrifter = new Film("High Plains Drifter", 1973, 7.7)
  val outlawJoseyWales = new Film("The Outlaw Josey Wales", 1976, 7.9)
  val unforgiven = new Film("Unforgiven", 1992, 8.3)
  val granTorino = new Film("Gran Torino", 2008, 8.2)
  val invictus = new Film("Invictus", 2009, 7.4)
  val predator = new Film("Predator", 1987, 7.9)
  val dieHard = new Film("Die Hard", 1988, 8.3)
  val huntForRedOctober = new Film("The Hunt for Red October", 1990, 7.6)
  val thomasCrownAffair = new Film("The Thomas Crown Affair", 1999, 6.8)
  val eastwood = new Director("Clint", "Eastwood", 1930,
    Seq(highPlainsDrifter, outlawJoseyWales, unforgiven, granTorino, invictus))
  val mcTiernan = new Director("John", "McTiernan", 1951,
    Seq(predator, dieHard, huntForRedOctober, thomasCrownAffair))
  val nolan = new Director("Christopher", "Nolan", 1970,
    Seq(memento, darkKnight, inception))
  val someGuy = new Director("Just", "Some Guy", 1990,
    Seq())
  val directors = Seq(eastwood, mcTiernan, nolan, someGuy)

  val task1 = (numberOfFilms: Int) => directors.filter(x=> x.films.size > numberOfFilms).map(d => d.firstName)
  println(task1(3))

  val task2 = (year: Int) => directors.filter(x=> x.yearOfBirth < year)
  println(task2(1931))

  val task3 = (year: Int, numberOfFilms: Int) => directors.filter(x=> x.films.size >numberOfFilms && x.yearOfBirth < year)
  println(task3(1970,4))

  def task4(asc: Boolean = true) = asc match {
    case true => directors.sortWith((x,y)=> x.yearOfBirth > y.yearOfBirth)
    case false => directors.sortWith((x,y)=> x.yearOfBirth < y.yearOfBirth)
  }
  println(task4(true))

  val task5 = nolan.films.map(nme=> nme.name)
  println(task5)

  println("////////////////////")
  val task6 = directors.map(x=>x.films.map(m=>m.name))
  println(task6.flatten)


  val task7 = mcTiernan.films.minBy(x=>x.yearOfRelease)
  println(task7)

  println("****************")
  val task8 = directors.map(x=>x.films).flatten.sortWith((a,b)=>a.imdbRating > b.imdbRating)
  println(task8)


  def task9():Double = {
    var sum, cnt: Double=0
    cnt = directors.flatMap(x=>x.films).map(m=>m.imdbRating).length
    directors.foreach(x=>x.films.foreach(r=>{
      sum+=r.imdbRating;
    }))
    println(sum)
    println(cnt)
    sum/cnt
  }
  println(task9())

  def task10():Unit={
    directors.foreach(m=>m.films.foreach(n=>{
      println(s"Tonight only!!! Film ${n.name} by ${m.firstName} ${m.lastName}")
    }))
  }
  task10()

  directors.foreach(x=>x.films.foreach(f=>{
    println(s"${x.firstName}   ${f.yearOfRelease}")
  }))
}

