package mlroads.datahack

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction
import org.apache.commons.math3.fitting._
import scala.math._

object XonYOffsetExtractor extends Extractor {

  val featureNames = List("XonYOffset")

  def getFeatures(track: List[Row]): List[Double] = {

    val obs = new WeightedObservedPoints;

    track.foreach { x => obs.add(x.x, x.y) }

    // Instantiate a second-degree polynomial fitter.
    val fitter = PolynomialCurveFitter.create(1)

    // Retrieve fitted parameters (coefficients of the polynomial function).
    val coeff = fitter.fit(obs.toList())
    val coeffList = coeff.toList

    coeffList

    // val func = new PolynomialFunction(coeff)
    val a = coeffList(1)
    val b = coeffList(0)

    val d = track.map { k =>
      val mone = a * k.x - k.y + b
      val mechane = Math.sqrt(Math.pow(a, 2) + 1)
      Math.abs(mone.toDouble / mechane)
    }

    List(d.sum)
    // val max = func.value(-b / (2 * a))
    // val root1 = (-b + math.sqrt(b * b - 4.0 * a * c)) / (2 * a)
    // val root2 = (-b - math.sqrt(b * b - 4.0 * a * c)) / (2 * a)
    // val duration = math.abs(root1 - root2)

    // List(max, duration, max / duration)
  }
}