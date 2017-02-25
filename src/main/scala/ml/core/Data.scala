package ml.core

import net.java.truecommons.shed.ResourceLoan._
import org.apache.commons.csv._
import java.io.{ File, PrintWriter }
import java.nio.charset._
import com.esotericsoftware.kryo.io._
import com.esotericsoftware.kryo.Kryo
import weka.core.converters._
import weka.core._
import com.univocity.parsers.csv.{
  CsvParser => UniCsvParser,
  CsvWriter,
  CsvWriterSettings,
  CsvParserSettings
}

import java.io.{ FileInputStream, FileOutputStream, File }
import ml._
import ml.core._
import scala.collection.JavaConverters._

object Data {

  private val basePath = new File(".").getCanonicalPath + "/data/"
  def pathOf(name: String) = s"${basePath}${name}"

  def extractCsv[A](path: String)(fields: String*)(extractor: Iterator[Array[String]] => A): A =
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

  def writeCsv[A](path: String)(header: String*)(data: Iterator[Seq[Any]]): Unit =
    loan(new FileOutputStream(path, false)) to { outputStream =>
      val writer = new CsvWriter(outputStream, new CsvWriterSettings());
      writer.writeHeaders(header.asJava)

      try {
        for (row <- data) {
          writer.writeRow(row.asInstanceOf[Seq[AnyRef]]: _*)
        }
      } finally {
        writer.close()
      }
    }
}