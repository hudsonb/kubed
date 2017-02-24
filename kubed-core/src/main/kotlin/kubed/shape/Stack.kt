package kubed.shape

import kubed.shape.offset.stackOffsetNone
import kubed.shape.order.*

fun <T, K> stack(keys: () -> List<K>, value: (d: T, key: K) -> Double, data: List<T>, order: (List<Series<*, *>>) -> List<Int> = stackOrderNone(),
                 offset: (List<Series<*, *>>, List<Int>) -> Unit = stackOffsetNone()): List<Series<T, K>> {
    val stack = Stack(keys, value)
    stack.order = order
    stack.offset = offset
    return stack(data)
}

fun <T, K> stack(keys: List<K>, value: (d: T, key: K) -> Double, order: (List<Series<*, *>>) -> List<Int> = stackOrderNone(),
                 offset: (List<Series<*, *>>, List<Int>) -> Unit = stackOffsetNone(), data: List<T>): List<Series<T, K>> {
    val stack = Stack({ keys }, value)
    stack.order = order
    stack.offset = offset
    return stack(data)
}

class Stack<T, K>(val keys: () -> List<K>, val value: (d: T, key: K) -> Double) {
    var order = stackOrderNone()
    var offset = stackOffsetNone()

    operator fun invoke(data: List<T>): List<Series<T, K>> {
        val kz = keys()
        val sz = ArrayList<Series<T, K>>(kz.size)

        for(i in kz.indices) {
            val ki = kz[i]
            val s = Series<T, K>(ki, i)
            for(j in data.indices) {
                s += Point(ki, data[j], 0.0, value(data[j], ki))
            }

            sz += s
        }

        val oz = order(sz)
        for(i in oz.indices) {
            sz[oz[i]].index = i
        }

        offset(sz, oz)
        return sz
    }

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            val data: List<Map<String, Int>> = listOf(mapOf(Pair("apples", 3840), Pair("bananas", 1920), Pair("cherries", 960), Pair("dates", 400)),
                              mapOf(Pair("apples", 1600), Pair("bananas", 1440), Pair("cherries", 960), Pair("dates", 400)),
                              mapOf(Pair("apples", 640), Pair("bananas", 960), Pair("cherries", 640), Pair("dates", 400)),
                              mapOf(Pair("apples", 320), Pair("bananas", 480), Pair("cherries", 640), Pair("dates", 400)))

            val test = stack<Map<String, Int>, String>({ listOf("apples", "bananas", "cherries", "dates" )}, { d: Map<String, Int>, k: String -> d[k]!!.toDouble() }, data)
            println("sip")
        }
    }
}

data class Point<T, K>(val key: K, val data: T, var y0: Double, var y1: Double)
data class Series<T, K>(val key: K, var index: Int) : ArrayList<Point<T, K>>()


