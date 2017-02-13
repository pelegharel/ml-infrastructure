package mlroads.datahack

import mlroads.datahack.Row

object Utils {
  def getPeakIndex(track: List[Row]): Int = {
    val maxHeight = track.map(_.z).max
    track.indexWhere(_.z == maxHeight)
  }
}