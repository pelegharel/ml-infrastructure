package mlroads.datahack

import scala.math._

object TotalDistanceExtractor extends Extractor {

  val featureNames = List("total_distance_traveled", "track_size")

  def getFeatures(track: List[Row]): List[Double] = {
    val (xHead, yHead) = (track.head.x, track.head.y)
    val (xLast, yLast) = (track.last.x, track.last.y)

    val xDiff = xLast - xHead
    val yDiff = yLast - yHead

    List(Math.sqrt(Math.pow(xDiff, 2) + Math.pow(yDiff, 2)), track.size.toDouble)
  }
}