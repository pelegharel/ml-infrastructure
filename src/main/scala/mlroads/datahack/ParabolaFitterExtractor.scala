package mlroads.datahack

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction
import org.apache.commons.math3.fitting._

object ParabolaFitterExtractor extends Extractor {

  val featureNames = List("para_maxPeak", "para_duration", "para_maxPeakDurationRation")

  def getFeatures(track: List[Row]): List[Double] = {

    val obs = new WeightedObservedPoints;
    val firstEpoch = track.head.timestamp.toEpochMilli

    track.foreach { x => obs.add((x.timestamp.toEpochMilli - firstEpoch).toDouble, x.z) }

    // Instantiate a second-degree polynomial fitter.
    val fitter = PolynomialCurveFitter.create(2)

    // Retrieve fitted parameters (coefficients of the polynomial function).
    val coeff = fitter.fit(obs.toList())
    val coeffList = coeff.toList
    val func = new PolynomialFunction(coeff)
    val a = coeffList(2)
    val b = coeffList(1)
    val c = coeffList(0)

    val max = func.value(-b / (2 * a))
    val root1 = (-b + math.sqrt(b * b - 4.0 * a * c)) / (2 * a)
    val root2 = (-b - math.sqrt(b * b - 4.0 * a * c)) / (2 * a)
    val duration = math.abs(root1 - root2)

    List(max, duration, max / duration)
  }
}