package watch.doge

import java.util.*
import kotlin.concurrent.fixedRateTimer

fun main() {
    var price = fetchPrice()
    notifyOnce(price)
    lastPrice = price
    val minute = Calendar.getInstance().get(Calendar.MINUTE)
    val nextMinute = when (minute) {
        in (0 until 15) -> 15
        in (15 until 30) -> 30
        in (30 until 45) -> 45
        else -> 60
    }
    fixedRateTimer(initialDelay = (nextMinute - minute).toLong() * 60 * 1000, period = 15.toLong() * 60 * 1000) {
        price = fetchPrice()
        notifyOnce(price)
        lastPrice = price
    }
}
