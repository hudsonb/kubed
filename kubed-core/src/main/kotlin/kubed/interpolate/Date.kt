package kubed.interpolate

import java.util.*

fun interpolateDate(a: Date, b: Date): (Double) -> Date {
    val d = b.time - a.time
    val date = Date()
    return { t -> date.time = (a.time + d * t).toLong(); date }
}