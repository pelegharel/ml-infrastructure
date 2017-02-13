package mlroads.datahack

object TotalDurationExtractor extends Extractor {

  val featureNames = List("total_duration_traveled")

  def getFeatures(track: List[Row]): List[Double] = {
    val totalDuration = java.time.Duration.between(
      track.head.timestamp,
      track.last.timestamp
    ).toMillis
    if (totalDuration > 0) {
      List(1000.0 / totalDuration)
    } else {
      List(0.0)
    }
  }
}