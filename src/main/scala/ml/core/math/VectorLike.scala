package ml.core.math

trait VectorLike[A] {
  def +(that: A): A
  def -(that: A): A
  def *(scalar: Double): A
}