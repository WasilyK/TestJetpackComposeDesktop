import java.time.LocalDateTime

data class Candle(
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val time: LocalDateTime
):Comparable<Candle> {
        override fun compareTo(other: Candle): Int = if(time.isBefore(other.time)) -1 else 1
}