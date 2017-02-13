package mlroads.datahack
import java.time.Duration
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D

object SampleExtractor extends Extractor {

  val featureNames = List("trackSize", "xSum")

  def getTrijectory(track: List[Row]) = {
    val startTime = track.head.timestamp
    track.map(x => (Duration.between(startTime, x.timestamp) -> new Vector3D(x.x, x.y, x.z)))
  }

  def getFeatures(track: List[Row]): List[Double] = {
    List(track.size, track.map(_.x).sum)
  }
}