package mlroads.core
import weka.core._

case class WekaStructure[TClass <: Enum[TClass]](
    name: String,
    attributes: Seq[Attribute],
    classValues: Array[_ <: TClass]
) {
  lazy val instances = {
    val vector = attributes.foldLeft(new FastVector()) { case (v, a) => v.addElement(a); v }
    val classAtrtribute = new Attribute(
      "class",
      classValues.map(_.toString).foldLeft(new FastVector()) { case (v, x) => v.addElement(x); v }
    )

    vector.addElement(classAtrtribute)
    val res = new Instances(name, vector, 0)
    res.setClassIndex(vector.size - 1)
    res
  }

  def newInstance(values: Array[_ <: Any], classValue: TClass): Instance = {
    val res = new Instance(attributes.length + 1)
    res.setDataset(instances)

    for (i <- (0 until attributes.size)) {
      values(i) match {
        case x: Double => res.setValue(i, x)
        case x: String => res.setValue(i, x)
        case _ => throw new IllegalArgumentException("not valid type")
      }
    }

    res.setClassValue(classValue.toString)
    res
  }
}