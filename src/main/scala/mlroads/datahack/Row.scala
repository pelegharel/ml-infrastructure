package mlroads.datahack
import java.time.Instant

case class Row(
  label: Int,
  timestamp: Instant,
  trajIndex: Int,
  x: Double,
  y: Double,
  z: Double
)