package mlroads.datahack

import mlroads.datahack.Utils

object PeakHeightExtractor extends Extractor {

  val featureNames = List("peak_height")

  def getFeatures(track: List[Row]): List[Double] = {
    val peakIndex = Utils.getPeakIndex(track)
    List(track(peakIndex).z)
  }
}