package mlroads.datahack
object SampleExtractor extends Extractor {

  val featureNames = List("trackSize", "xSum")

  def getFeatures(track: List[Row]): List[Double] = {
    List(track.size, track.map(_.x).sum)
  }
}