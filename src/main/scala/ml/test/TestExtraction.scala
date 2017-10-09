package ml.test

import ml.core.{ Data, FeatureExtractor, Extractor }

case class TestData(x: Double, y: Double)

object ExtractionTest {
  def extractGroup(line: Seq[String]) = {
    line match {
      case Seq(a, b, _, _) => (a, b)
    }
  }

  def getData(line: Seq[String]) = {
    line match {
      case Seq(_, _, x, y) => TestData(x.toDouble, y.toDouble)
    }
  }

  object F1 extends Extractor[TestData] {
    val header = Seq("f1")
    def extract(rows: Seq[TestData]) = Seq(rows.map(d => d.x + d.y).sum)
  }

  object F2 extends Extractor[TestData] {
    val header = Seq("f2")
    def extract(rows: Seq[TestData]) = Seq(rows.map(d => d.x * d.y).sum)
  }

  object F3 extends Extractor[TestData] {
    val header = Seq("length")
    def extract(rows: Seq[TestData]) = Seq(rows.length)
  }

  def test = {
    FeatureExtractor.extractFeatures(
      extractor = Data.extractCsv(Data.pathOf("test.csv"))("key1", "key2", "data1", "data2"),
      writer = Data.writeCsv(Data.pathOf("testFeature.csv")),
      key = { case Seq(a, b, _, _) => (a, b) },
      row = getData)(F1, F2, F3)
  }
}