package Algebra

sealed trait Water

case class Well() extends Water
case class Spring() extends Water
case class Tap() extends  Water
//Sum type pattern

case class BottledWater(size: Int, source: Water, carbonated: Boolean)
//BottledWater has size(of type Int) and source(type of Water) and carbonated(type of Boolean)
//product type pattern