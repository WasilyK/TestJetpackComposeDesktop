import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Size
import kotlin.math.roundToInt

class ChartState(val candles: List<Candle>) {
    val candleWidth = mutableStateOf(6f)
    val scrollOffset = mutableStateOf(0f)
    val distanceBetweenCandles = derivedStateOf {
        when(candleWidth.value) {
            in 1f..4f -> 1f
            in 5f..8f -> 2f
            in 9f..12f -> 3f
            in 13f..16f -> 4f
            in 17f..20f -> 5f
            in 21f..24f -> 6f
            in 25f..30f -> 7f
            else -> 1f
        }
    }
    var size: Size = Size(0f,0f)
    val visibleCandlesCount = derivedStateOf {
        (size.width / (distanceBetweenCandles.value + candleWidth.value)).roundToInt()
    }
    val visibleCandles = derivedStateOf {
        if(visibleCandlesCount.value >= candles.size) {
            candles
        } else {
            candles.subList(
                scrollOffset.value.roundToInt().coerceAtLeast(0).coerceAtMost(candles.size - 1),
                (scrollOffset.value.roundToInt() + visibleCandlesCount.value - 1).coerceAtMost(candles.size - 1).coerceAtLeast(0))
        }
    }
    val scroll: (Float) -> Unit = {
        scrollOffset.value = (scrollOffset.value + it)
            .coerceAtLeast(0f)
            .coerceAtMost(
                (candles.size - visibleCandlesCount.value / 2).toFloat()
            )
    }
    val transformableState = TransformableState {zoomChange, _, _ ->
        if(zoomChange > 0) {
            if(candleWidth.value < 30f) {
                candleWidth.value += zoomChange
            }
        } else {
            if(candleWidth.value > 2f) {
                candleWidth.value += zoomChange
            }
        }
    }

    val maxPrice = derivedStateOf {
        visibleCandles.value.maxOf { it.high }.toFloat()
    }
    val minPrice = derivedStateOf {
        visibleCandles.value.minOf { it.low }.toFloat()
    }
    val step: State<Float> = derivedStateOf {
        size.height / (maxPrice.value - minPrice.value)
    }
}