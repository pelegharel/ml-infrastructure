package mlroads.datahack

import mlroads.datahack.Utils

object TimeCategoryExtractor extends Extractor {

  val featureNames = List("timeCategory")

  def getFeatures(track: List[Row]): List[Double] = {

    val zone = java.time.ZonedDateTime.now.getZone
    val hour = track.map(x => x.timestamp.atZone(zone).getHour).head

    List(hour / 6)
  }
}