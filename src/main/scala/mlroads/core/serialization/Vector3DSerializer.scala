package mlroads.core.serialization

import com.esotericsoftware.kryo.Serializer
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D
import com.esotericsoftware.kryo.io._
import com.esotericsoftware.kryo.Kryo

class Vector3DSerializer extends Serializer[Vector3D] {
  def write(kryo: Kryo, output: Output, obj: Vector3D): Unit = {
    output.writeDoubles(Array(obj.getX, obj.getY, obj.getZ))
  }

  def read(kryo: Kryo, input: Input, objType: Class[Vector3D]): Vector3D = {
    val coords = input.readDoubles(3)
    new Vector3D(coords(0), coords(1), coords(2))
  }
}