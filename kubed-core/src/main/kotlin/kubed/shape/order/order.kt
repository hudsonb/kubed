package kubed.shape.order

import kubed.shape.Series
import kubed.util.isTruthy
import java.util.*

fun stackOrderNone(): (List<Series<*, *>>) -> List<Int> = { series ->
    var i = 0
    series.map { i++ }
}

fun stackOrderAscending(): (List<Series<*, *>>) -> List<Int> = { series ->
    val sums = series.map(::sum)
    stackOrderNone()(series).sortedWith(Comparator { a, b -> (sums[a] - sums[b]).toInt() })
}

fun stackOrderDescending(): (List<Series<*, *>>) -> List<Int> =  { series ->
    stackOrderAscending()(series).reversed()
}

fun stackOrderInsideOut(): (List<Series<*, *>>) -> List<Int> = { series ->
    val sums = series.map(::sum)
    val o = stackOrderNone()(series).sortedWith(Comparator { a, b -> (sums[b] - sums[a]).toInt() })
    var top = 0.0
    var bottom = 0.0
    val tops = ArrayList<Int>()
    val bottoms = ArrayList<Int>()

    series.indices
            .asSequence()
            .map { o[it] }
            .forEach {
                if(top < bottom) {
                    top += sums[it]
                    tops += it
                }
                else {
                    bottom += sums[it]
                    bottoms += it
                }
            }

    bottoms.asReversed() += tops
    bottoms
}

private fun sum(series: Series<*, *>): Double {
    var s = 0.0
    series.asSequence()
            .map { it.y1 }
            .filter(Double::isTruthy)
            .forEach { s += it }

    return s
}
