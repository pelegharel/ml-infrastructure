package mlroads.datahack
import mlroads.core.Data
import java.time.Instant
import java.time.Duration
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import org.apache.commons.math3.geometry.euclidean.threed.{ Vector3D, Rotation }

object DataExtractor {
  def fields = List("label", "time_stamp", "traj_ind", "x", "y", "z")

  def extract[A](path: String)(extractor: Iterator[Row] => A) = {
    Data.extractCsv(path)(fields: _*) { it =>
      val mapped = it.map { r =>
        Row(
          r(0).toInt,
          Instant.parse(r(1).updated(10, 'T') + 'Z'),
          r(2).toInt,
          r(3).toDouble,
          r(4).toDouble,
          r(5).toDouble
        )
      }
      extractor(mapped)
    }
  }

  def getTracks(path: String, filter: Boolean, paf: Boolean = false) = {
    val res = extract(path)(_.toList.groupBy(_.trajIndex).
      filter { case (_, track) => !filter || track.size >= 10 }.
      mapValues(_.sortBy(_.timestamp))).toList
    if (!paf) {
      res
    } else {
      val rotation = new Rotation(Vector3D.PLUS_K, 0.25 * scala.math.Pi * 2)

      res.flatMap {
        case (key, rows) =>
          val original = key -> rows
          val paffed = rows.drop(3).sliding(14, 6).map { x => key -> x }.toList
          val res = original :: paffed
          res.map {
            case (key, rows) =>
              val newRows = rows.map { x =>
                val vec = new Vector3D(x.x, x.y, x.z)
                val vecRot = rotation.applyTo(vec)
                x.copy(x = vecRot.getX, y = vecRot.getY, z = vecRot.getZ)
              }
              (key, newRows)
          }
      }
    }
  }

  def extractFeatures(data: List[(Int, List[Row])], extractors: List[Extractor]) = {
    val names = "id" :: extractors.flatMap(_.featureNames) ::: "label" :: Nil
    val res = data.par.map {
      case (id, track) =>
        id.toDouble :: extractors.par.flatMap(_.getFeatures(track)).toList ::: track.head.label :: Nil
    }
    (names, res)
  }

  def getTrainData = getTracks(Data.pathOf("train.csv"), false, true)
  def getTestData = getTracks(Data.pathOf("test.csv"), false)

  def run = {
    Seq(getTrainData -> "featuresTrain.csv", getTestData -> "featuresTest.csv").foreach {
      case (data, fileName) =>
        val (header, features) = extractFeatures(data, List(
          FFTExtractor,
          ParabolaFitterExtractor,
          TotalDistanceExtractor,
          XVelocityExtractor,
          TotalDurationExtractor,
          TimeCategoryExtractor
        ))

        Data.writeCsv(Data.pathOf(fileName), header: _*) { printer =>
          features.toList.foreach { f =>
            val currLable = f.last match {
              case 0 => "a"
              case 1 => "b"
              case 2 => "c"
            }

            val ff = f.dropRight(1) ::: currLable :: Nil
            printer.printRecord(ff.toIterable.asJava)
          }
        }
    }

  }
}