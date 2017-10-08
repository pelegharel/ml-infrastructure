package ml.core
import scala.util.Try

object FeatureExtractor {
  def extractFeatures[Key, Row](
    extractor: (Iterator[Seq[String]] => Unit) => Unit,
    writer: (Seq[String]) => (Iterator[Seq[Any]] => Unit),
    key: (Seq[String] => Key),
    row: (Seq[String] => Row))(
    features: (Seq[String], Seq[Row] => Seq[Any])*) = {

    val featuresHeader = features.flatMap(_._1)
    val featureExtractors = features.map(_._2)

    val writerWithHeaders = writer(featuresHeader)

    extractor { it =>
      val buffered = it.map(x => key(x) -> row(x)).buffered

      val gropuedIt = Iterator.continually {
        Try({
          val (currKey, _) = buffered.head

          buffered.takeWhile {
            case (key, _) => key == currKey
          }.map {
            case (_, row) => row
          }.toList
        })
      }.takeWhile(_.isSuccess).map(_.get)

      val featuresIt = gropuedIt.map { rows =>
        featureExtractors.flatMap(_(rows))
      }

      writerWithHeaders(featuresIt)
    }

  }
}