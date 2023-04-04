import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import kotlin.math.abs

@OptIn(DelicateCoroutinesApi::class, ExperimentalComposeUiApi::class)
@Composable
@Preview
fun App() {

    MaterialTheme {
        val state = remember { ChartState(getData(
            start = LocalDateTime.of(2020, 9, 1, 10, 0,0),
            end = LocalDateTime.of(2020, 9, 10, 15, 0, 0)
        )) }

        Column(Modifier.fillMaxSize()) {
            Row(Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(red = 96, green = 125, blue = 139)),
                verticalAlignment = Alignment.CenterVertically) {

                Spacer(Modifier
                    .fillMaxHeight()
                    .width(20.dp))

                Button(modifier = Modifier
                    .width(40.dp)
                    .height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(38,50,56),
                        contentColor = Color.White
                    ),
                    onClick = {
                        GlobalScope.launch {
                            state.transformableState.transform {
                                transformBy(1f)
                            }
                        }
                    }) {

                    Text("+")
                }

                Spacer(Modifier
                    .fillMaxHeight()
                    .width(5.dp))

                Button(modifier = Modifier
                    .width(40.dp)
                    .height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(38,50,56),
                        contentColor = Color.White),
                    onClick = {
                        GlobalScope.launch {
                            state.transformableState.transform {
                                transformBy(-1f)
                            }
                        }

                    }) {

                    Text("-")
                }
            }

            Canvas(modifier = Modifier
                .fillMaxWidth()
                .weight(9f)
                .transformable(state.transformableState)
                .onPointerEvent(PointerEventType.Scroll) {
                    state.scroll(it.changes.first().scrollDelta.y)
                }) {

                state.size = this.size

                state.visibleCandles.value.forEachIndexed {index, candle ->
                    val color = if(candle.open > candle.close) { Color.Red } else { Color.Green }

                    fun xOffset(i: Int): Float =
                        state.distanceBetweenCandles.value + state.candleWidth.value / 2 + (state.candleWidth.value + state.distanceBetweenCandles.value) * i
                    drawLine(
                        color = color,
                        start = Offset(xOffset(index), (state.maxPrice.value - candle.high.toFloat()) * state.step.value),
                        end = Offset(xOffset(index), (state.maxPrice.value - candle.low.toFloat()) * state.step.value)
                    )

                    fun yOffset(): Float = if(candle.open > candle.close) { state.maxPrice.value - candle.open.toFloat() } else { state.maxPrice.value - candle.close.toFloat() }
                    drawRect(
                        color = color,
                        topLeft = Offset(xOffset(index) - state.candleWidth.value / 2, yOffset() * state.step.value),
                        size = Size(state.candleWidth.value, abs(candle.open.toFloat() - candle.close.toFloat()) * state.step.value)
                    )
                }
            }
        }

    }
}

fun main() = application {

    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}

fun getData(start: LocalDateTime? = null, end: LocalDateTime? = null): List<Candle> {
    val candles = mutableListOf<Candle>()
    object{}.javaClass.getResource("./quotes.txt")?.let { url ->
        Files.readAllLines(Path.of(url.toURI()))
            .stream()
            .forEach { line ->
                val splitStr = line.split(" ")
                val year = splitStr[0].substring(0, 4).toInt()
                val month = splitStr[0].substring(4, 6).toInt()
                val day = splitStr[0].substring(6, 8).toInt()
                val hour = splitStr[1].substring(0, 2).toInt()
                val minute = splitStr[1].substring(2, 4).toInt()

                val dateTime = LocalDateTime.of(year, month, day, hour, minute)
                val open = splitStr[2].toDouble()
                val high = splitStr[3].toDouble()
                val low = splitStr[4].toDouble()
                val close = splitStr[5].toDouble()

                if((start != null) and (end != null)) {
                    if((dateTime.isAfter(start) and dateTime.isBefore(end)) or (dateTime.equals(start) or dateTime.equals(end))) {
                        candles.add(Candle(open, high, low, close, dateTime))
                    }
                } else {
                    candles.add(Candle(open, high, low, close, dateTime))
                }
            }
    }
    candles.sort()
    return candles
}
