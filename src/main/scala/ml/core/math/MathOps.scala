package ml.core.math

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D
import org.apache.commons.math3.complex.Complex
import ml.core.math.VectorLike
import scala.language.implicitConversions

object MathOps {
  implicit class Vector3DOps(v: Vector3D) extends VectorLike[Vector3D] {
    def unary_- = v.negate
    def +(that: Vector3D): Vector3D = v.add(that)
    def -(that: Vector3D): Vector3D = v.add(that.negate)
    def *(scalar: Double): Vector3D = v.scalarMultiply(scalar)
  }

  implicit class ScalarOps(scalar: Double) {
    def *(v: Vector3D): Vector3D = v.scalarMultiply(scalar)
  }

  implicit class ComplexOps(c: Complex) {
    def *(other: Complex) = c.multiply(other)
    def absSqr = (c * c.conjugate).getReal
    def toArray = Array(c.getReal, c.getImaginary)
  }

  implicit def realToComplex(x: Double) = new Complex(x, 0)
}