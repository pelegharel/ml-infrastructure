package mlroads.datahack
import java.time.Duration
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D
import mlroads.core.time.TimeSeries
import mlroads.core.math.MathOps._
import mlroads.core.time.TimeOps._
import org.apache.commons.math3.transform.FastFourierTransformer
import org.apache.commons.math3.transform.DftNormalization._
import org.apache.commons.math3.transform.TransformType._

object FFTExtractor extends Extractor {
  val powSize = 6
  val pointNum = 2 << powSize
  val featureNum = 3

  def getTransform = new FastFourierTransformer(STANDARD)

  val featureNames = List("x", "y", "z").flatMap { coord =>
    (1 until featureNum * 2).map { i => s"fft_${coord}_$i" }.toList
  }

  def getTimeSeries(track: List[Row]) = {
    val startTime = track.head.timestamp
    val trijectory = track.map(x =>
      (Duration.between(startTime, x.timestamp) -> new Vector3D(x.x, x.y, x.z)))
    TimeSeries(trijectory)
  }

  def getConstantNumPoints(track: List[Row]) = {
    val ts = getTimeSeries(track)
    val step = ts.totalDuration / (pointNum - 1)
    ts((0 until pointNum).map(_ * step))
  }

  def getFeatures(track: List[Row]): List[Double] = {
    if (track.size < 10) {
      return featureNames.map(_ => 0.0)
    }
    val fft = new FastFourierTransformer(STANDARD)

    val points = getConstantNumPoints(track)

    List(points.map(_.getX), points.map(_.getY), points.map(_.getZ)).flatMap { coordSeries =>
      fft.transform(coordSeries.toArray, FORWARD).take(featureNum).
        flatMap(x => Seq(x.getImaginary, x.getReal)).
        drop(1).
        toList
    }
  }
}