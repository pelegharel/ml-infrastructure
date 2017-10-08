package ml.test

import ml.core.{ Data, FeatureExtractor }

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

  val header = Seq("f1", "f2", "f3")

  def f1(ds: Seq[TestData]) = Seq(ds.map(d => d.x + d.y).sum)

  def f2(ds: Seq[TestData]) = Seq(ds.map(d => d.x * d.y).sum)

  def f3(ds: Seq[TestData]) = Seq(ds.map(d => 2 * d.x + d.y).sum)

  def test = {
    FeatureExtractor.extractFeatures(
      extractor = Data.extractCsv(Data.pathOf("test.csv"))("key1", "key2", "data1", "data2"),
      writer = Data.writeCsv(Data.pathOf("testFeature.csv")),
      key = { case Seq(a, b, _, _) => (a, b) },
      row = getData)(
        Seq("f1") -> f1,
        Seq("f2") -> f2,
        Seq("f3") -> f3)
  }
}