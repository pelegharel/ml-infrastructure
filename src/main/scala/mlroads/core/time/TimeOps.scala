package mlroads.core.time

import java.time._
import java.time.temporal.Temporal
import scala.math.Ordered

object TimeOps {
  implicit class TemporalOps(temp: Temporal) {
    def -(that: Temporal): Duration = Duration.between(that, temp)
  }

  implicit class DurationOps(duration: Duration) extends Ordered[Duration] {
    def +(that: Duration): Duration = duration.plus(that)
    def -(that: Duration): Duration = duration.minus(that)
    def *(scalar: Long): Duration = duration.multipliedBy(scalar)
    def /(other: Duration): Double = duration.toNanos.toDouble / other.toNanos
    def /(scalar: Long): Duration = duration.dividedBy(scalar)
    def unary_- = duration.negated
    def isPositive = !(duration.isNegative || duration.isZero)
    def compare(that: Duration): Int = duration.compareTo(that)
  }

  implicit class LongOps[T <% Long](num: T) {
    def *(duration: Duration): Duration = duration.multipliedBy(num)
  }
}
