package mlroads.core.serialization

import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io._
import com.esotericsoftware.kryo.Kryo
import scala.reflect.ClassTag
import Utils._

class ToupleSerializer[A: ClassTag, B: ClassTag] extends Serializer[(A, B)] {
  def write(kryo: Kryo, output: Output, obj: (A, B)): Unit = {
    obj match {
      case (a, b) =>
        kryo.writeObject(output, a)
        kryo.writeObject(output, b)
    }
  }
  val (aClass, bClass) = (runtimeClassOf[A], runtimeClassOf[B])

  def read(kryo: Kryo, input: Input, objType: Class[(A, B)]): (A, B) = {
    val a = kryo.readObject(input, aClass)
    val b = kryo.readObject(input, bClass)
    (a, b)
  }

}