package ml.core
import scala.util.Try
  
trait Extractor[A] {
  val header: Seq[String]
  def extract(rowGroup: Seq[A]): Seq[Any]
}

object FeatureExtractor {
  def extractFeatures[Key, Row](
    extractor: (Iterator[Seq[String]] => Unit) => Unit,
    writer: (Seq[String]) => (Iterator[Seq[Any]] => Unit),
    key: (Seq[String] => Key),
    row: (Seq[String] => Row))(
    features: Extractor[Row]*) = {

    extractor { it =>

      val gropuedIt = {
        val buffered = it.map(x => key(x) -> row(x)).buffered

        Iterator.continually {
          Try({
            val (currKey, _) = buffered.head
            buffered.takeWhile {
              case (key, _) => key == currKey
            }.map {
              case (_, row) => row
            }.toList
          })
        }.takeWhile(_.isSuccess).map(_.get)
      }

      val featuresIt = gropuedIt.map { rows =>
        features.flatMap(_.extract(rows))
      }       

      writer(features.flatMap(_.header))(featuresIt)
    }

  }
}