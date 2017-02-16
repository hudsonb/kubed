package kubed.shape

import kubed.shape.offset.stackOffsetNone
import kubed.shape.order.*

fun <T, K> stack() = Stack<T, K>()
fun <T, K> stack(init: Stack<T, K>.() -> Stack<T, K>) {
    val stack = Stack<T, K>()
    stack.init()
}

class Stack<T, K> {
    var keys: () -> List<K> = { emptyList<K>() }
    var value: (d: T, key: K) -> Double = { _, _ -> throw IllegalStateException("value must be specified") }
    var order = stackOrderNone()
    var offset = stackOffsetNone()

    operator fun invoke(data: List<T>): List<Series<T, K>> {
        val kz = keys()
        val sz = ArrayList<Series<T, K>>(kz.size)

        for(i in kz.indices) {
            val ki = kz[i]
            val points = ArrayList<Point<T>>(data.size)
            for(j in data.indices) {
                points += Point(data[j], 0.0, value(data[j], ki))
            }

            sz += Series(ki, i, points)
        }

        val oz = order(sz)
        for(i in oz.indices) {
            sz[oz[i]].index = i
        }

        offset(sz, oz)
        return sz
    }
}

data class Point<T>(val data: T, var y0: Double, var y1: Double)
data class Series<T, K>(val key: K, var index: Int, val points: List<Point<T>>) {
    val size = points.size
    operator fun get(i: Int): Point<T> = points[i]
}


