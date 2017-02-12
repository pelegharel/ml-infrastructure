package mlroads.core

import net.java.truecommons.shed.ResourceLoan._
import org.apache.commons.csv._
import java.io.{ File, PrintWriter }
import java.nio.charset._
import com.esotericsoftware.kryo.io._
import com.esotericsoftware.kryo.Kryo
import weka.core.converters._
import weka.core._
import com.univocity.parsers.csv.{ CsvParser => UniCsvParser, CsvParserSettings }

import java.io.{ FileInputStream, FileOutputStream }
import mlroads._
import mlroads.core._

object Data {
  private val basePath = new File(".").getCanonicalPath + "/data/"

  def pathOf(name: String) = s"${basePath}${name}"

  def gyroPath = pathOf("Phones_gyroscope.csv")
  def accPath = pathOf("Phones_accelerometer.csv")

  def getParser(path: String) = CSVParser.parse(new File(path), StandardCharsets.UTF_8, CSVFormat.DEFAULT.withHeader())
  def all(path: String) = loan(getParser(path)) to (_.getRecords())
  def extract[A](path: String)(extractor: CSVParser => A) = loan(getParser(path)) to (extractor(_))

  def extractCsv[A](path: String, fields: Array[String])(extractor: Iterator[Array[String]] => A): A =
    loan(new FileInputStream(path)) to { inputStream =>
      val settings = new CsvParserSettings()
      settings.setHeaderExtractionEnabled(true)
      settings.selectFields(fields: _*)
      val parser = new UniCsvParser(settings)
      parser.beginParsing(inputStream)

      try {
        val iterator = Iterator.continually(parser.parseNext).
          takeWhile(_ != null)
        extractor(iterator)
      } finally {
        parser.stopParsing()
      }
    }

  def writeCsv[A](path: String, header: String*)(action: CSVPrinter => A) = loan(new PrintWriter(path)) to { w =>
    loan(CSVFormat.DEFAULT.withHeader(header: _*).print(w)) to (action(_))
  }

  def getKryoInput(path: String) = new Input(new FileInputStream(path))

  def getOutputStream(path: String) = {
    val file = new File(path)
    file.getParentFile().mkdirs()
    new FileOutputStream(file)
  }

  def getKryoOutput(path: String) = new Output(getOutputStream(path))

  def writeKryo[A](path: String)(action: Output => A) = loan(getKryoOutput(path)) to { o =>
    action(o)
  }

  def readKryo[A](path: String)(action: Input => A) = loan(getKryoInput(path)) to { i =>
    action(i)
  }

  def getKryoIterator[A](kryo: Kryo, in: Input, readClass: Class[A]): Iterator[A] = {
    if (in.eof()) {
      Iterator.empty
    } else {
      Iterator.continually(kryo.readObject(in, readClass)).takeWhile(_ => !in.eof())
    }
  }

  def iterateKryo[A, B](kryo: Kryo, readClass: Class[A], paths: String*)(action: Iterator[A] => B): B = {
    def inner(paths: List[String], it: Iterator[A]): B = {
      paths match {
        case Nil => action(it)
        case head :: tail => readKryo(head) { in =>
          inner(tail, it ++ getKryoIterator(kryo, in, readClass))
        }
      }
    }
    inner(paths.toList, Iterator.empty)
  }

  def writeArff[A](structure: WekaStructure[_], path: String)(action: ArffSaver => A): A = {
    loan(getOutputStream(path)) to { os =>

      val saver = new ArffSaver() {
        {
          setStructure(structure.instances)
          setDestination(os)
          setRetrieval(Saver.INCREMENTAL)
        }
      }

      action(saver)
    }
  }

  def writeArff(path: String, structure: WekaStructure[_], data: Iterator[Instance]): Unit = {
    writeArff(structure, path) { saver =>
      data.foreach(saver.writeIncremental(_))
      saver.writeIncremental(null)
    }
  }
}