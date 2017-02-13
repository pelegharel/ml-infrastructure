package mlroads.datahack
import mlroads.core.Data
import java.time.Instant
import java.time.Duration
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

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
    extract(path)(_.toList.groupBy(_.trajIndex).
      filter { case (_, track) => track.size >= 10 }.
      mapValues(_.sortBy(_.timestamp))).toList
  }

  def extractFeatures(data: List[(Int, List[Row])], extractors: List[Extractor]) = {
    val names = "id" :: extractors.flatMap(_.featureNames) ::: "label" :: Nil
    val res = data.par.map {
      case (id, track) =>
        id.toDouble :: extractors.par.flatMap(_.getFeatures(track)).toList ::: track.head.label :: Nil
    }
    (names, res)
  }

  def getData = getTracks(Data.pathOf("train.csv"))

  def run = {
    val data = getData
    val (header, features) = extractFeatures(data, List(FFTExtractor))

    Data.writeCsv(Data.pathOf("res.csv"), header: _*) { printer =>
      features.toList.foreach { f =>
        printer.printRecord(f.toIterable.asJava)
      }
    }

    (header, features)
  }
}