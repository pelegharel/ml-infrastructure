package mlroads.core.serialization
import scala.reflect.ClassTag
import scala.reflect._

object Utils {
  def runtimeClassOf[A: ClassTag]() = classTag[A].runtimeClass.asInstanceOf[Class[A]]
}