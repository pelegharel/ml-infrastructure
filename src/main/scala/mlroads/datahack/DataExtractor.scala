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
}