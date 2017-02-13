package mlroads.datahack
import mlroads.core.Data
import java.time.Instant
object DataExtractor {
  def fields = List("label", "time_stamp", "traj_ind", "x", "y", "z")

  def extract[A](path: String)(extractor: Iterator[Row] => A) = {
    Data.extractCsv(path)(fields: _*) { it =>
      val mapped = it.map(r =>
        Row(
          r(0).toInt,
          Instant.parse(r(1).updated(10, 'T') + 'Z'),
          r(2).toInt,
          r(3).toDouble,
          r(4).toDouble,
          r(5).toDouble
        ))
      extractor(mapped)
    }
  }

  def getTracks(path: String) = {
    extract(path)(_.toList.groupBy(_.trajIndex).mapValues(_.sortBy(_.timestamp))).toList
  }

  def extractFeatures(data: List[(Int, List[Row])], extractors: List[Extractor]) = {
    val names = "id" :: extractors.flatMap(_.featureNames)
    val res = data.par.map {
      case (id, track) =>
        id :: extractors.par.flatMap(_.getFeatures(track)).toList
    }
    (names, res)
  }

  def run = {
    val data = getTracks(Data.pathOf("train.csv"))
    extractFeatures(data, List(SampleExtractor))
  }
}