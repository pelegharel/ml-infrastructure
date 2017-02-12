package mlroads.core

trait VectorLike[A] {
  def +(that: A): A
  def -(that: A): A
  def *(scalar: Double): A
}