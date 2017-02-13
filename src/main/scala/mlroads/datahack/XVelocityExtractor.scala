package mlroads.datahack

import java.time.Duration
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D
import mlroads.core.time.TimeSeries
import mlroads.core.math.MathOps._
import mlroads.core.time.TimeOps._
import scala.math._

object XVelocityExtractor extends Extractor {

  val featureNames = List("x", "y", "z").flatMap { coord =>
    List(s"max_speed_$coord", s"min_speed_$coord", s"avg_speed_$coord", s"std_speed_$coord")
  }

  def getVelocities(track: List[Row]) = {
    def inner(track: List[(Vector3D, Duration)], res: List[Vector3D]): List[Vector3D] = {
      track match {
        case head :: next :: tail if (head._1 == next._1) || (head._2 == next._2) =>
          inner(next :: tail, res :+ new Vector3D(0, 0, 0))
        case head :: next :: tail =>
          val v = (next._1 - head._1) * (1000.0 / (next._2 - head._2).toMillis.toDouble)
          inner(next :: tail, res :+ v)
        case _ => res
      }
    }
    inner(track.map(x => new Vector3D(x.x, x.y, x.z) -> Duration.between(track.head.timestamp, x.timestamp)), List.empty)
  }

  def getFeatures(track: List[Row]): List[Double] = {
    if (track.size < 4) {
      return featureNames.map(_ => 0.0)
    }

    val vs = getVelocities(track)
    List(vs.map(_.getX), vs.map(_.getY), vs.map(_.getZ)).flatMap { v =>
      val max = v.max
      val min = v.min
      val avg = v.sum / v.size
      val std = v.map(x => Math.pow(2, x - avg)).sum / v.size
      List(max, min, avg, std)
    }
  }
}