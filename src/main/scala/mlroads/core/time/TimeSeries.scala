package mlroads.core.time

import java.time.Duration
import mlroads.core.time.TimeOps._
import mlroads._
import mlroads.core._

class TimeSeries[A <% VectorLike[A]] private[TimeSeries] (series: Array[(Duration, A)]) {

  lazy val totalDuration = if (!series.isEmpty) series.last._1 - series.head._1 else Duration.ZERO

  private def calcVector(t: Duration, start: (Duration, A), end: (Duration, A)): A = {
    (start, end) match {
      case ((tStart, vStart), (tEnd, vEnd)) =>
        val frac = (t - tStart) / (tEnd - tStart)
        vStart + (vEnd - vStart) * frac
    }
  }

  def apply(t: Duration): A = apply(Seq(t)).head

  /**
   * Get values for given time seq.
   * Assumes timeSeq is sorted
   */
  def apply(timeSeq: Seq[Duration]): Seq[A] = apply(timeSeq, 0)._1

  def apply(timeSeq: Seq[Duration], startIndex: Int): (Seq[A], Int) = {
    val indexes = timeSeq.scanLeft(Duration.ZERO -> startIndex) {
      case ((_, i), t) =>
        t -> series.indexWhere({ case (time, _) => time > t }, i)
    } drop 1

    (indexes map {
      case (t, -1) => throw new IndexOutOfBoundsException(s"Duration ${t} out of time series range, max duration is ${series.last._1}")
      case (t, 0) =>
        calcVector(t, Duration.ZERO -> series(0)._2, series(0))
      case (t, i) => calcVector(t, series(i - 1), series(i))
    }, indexes.last._2)
  }
}

object TimeSeries {
  /**
   * Creates new time Series,
   * Assumes series is sorted
   */
  def apply[A <% VectorLike[A]](series: Seq[(Duration, A)]) = new TimeSeries(series.toArray)
}