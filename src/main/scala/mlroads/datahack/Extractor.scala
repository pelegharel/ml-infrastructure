package mlroads.datahack
trait Extractor {
  def featureNames: List[String]
  def getFeatures(track: List[Row]): List[Double]
}