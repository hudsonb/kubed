package kubed.shape

import kubed.util.MoreMath
import java.util.*

class Pie<T> {
    var value: (d: T, i: Int, data: List<T>) -> Double = { d, i, data -> d as Double }
    var sort: Comparator<T>? = null
    var sortValues: Comparator<Double>? = Comparator { a, b -> (a - b).toInt() }
        set(value) {
            sortValues = value
            sort = null
        }
    var startAngle: () -> Double = { 0.0 }
    var endAngle: () -> Double = { MoreMath.TAU }
    var padAngle: () -> Double = { 0.0 }

    fun value(func: (T, Int, List<T>) -> Double): Pie<T> {
        value = func
        return this
    }

    fun value(v: Double): Pie<T> {
        value = { _, _, _ -> v }
        return this
    }

    fun sort(c: Comparator<T>): Pie<T> {
        sort = c
        return this
    }

    fun sortValues(c: Comparator<Double>): Pie<T> {
        sortValues = c
        return this
    }

    fun startAngle(f: () -> Double): Pie<T> {
        startAngle = f
        return this
    }

    fun startAngle(value: Double): Pie<T> {
        startAngle = { value }
        return this
    }

    fun endAngle(f: () -> Double): Pie<T> {
        endAngle = f
        return this
    }

    fun endAngle(value: Double): Pie<T> {
        endAngle = { value }
        return this
    }

    fun padAngle(f: () -> Double): Pie<T> {
        padAngle = f
        return this
    }

    fun padAngle(value: Double): Pie<T> {
        padAngle = { value }
        return this
    }

    operator fun invoke(data: List<T>): List<PieWedge<T>> {
        val n = data.size
        var sum = 0.0
        val index = IntArray(n)
        val values = DoubleArray(n)
        val wedges = ArrayList<PieWedge<T>>(n)
        var a0 = startAngle()
        val da = Math.min(MoreMath.TAU, Math.max(-MoreMath.TAU, endAngle() - a0))
        val p = Math.min(Math.abs(da) / n, padAngle())
        val pa = p * (if(da < 0) -1 else 1)

        for(i in data.indices) {
            index[i] = i
            val v = value(data[i], i, data)
            values[index[i]] = v
            if(v > 0.0)
                sum += v
        }

        // Optionally sort the arcs by previously-computed values or by data.
        if(sortValues != null)
            index.sortedWith(Comparator { i, j ->  sortValues!!.compare(values[i], values[j]) })
        else if(sort != null)
            index.sortedWith(Comparator { i, j -> sort!!.compare(data[i], data[j]) })

        // Compute the arcs! They are stored in the original data's order.
        val k = if(sum > 0.0) da - n * pa / sum else 0.0
        for(i in index.indices) {
            val j = index[i]
            val v = values[j]
            val a1 = a0 + (if(v > 0) v * k else 0.0) + pa
            wedges[j] = PieWedge(data[j], v, i, a0, a1, p)
            a0 = a1
        }

        return wedges
    }
}

data class PieWedge<out T>(val data: T,
                           val value: Double,
                           val index: Int,
                           val startAngle: Double,
                           val endAngle: Double,
                           val padAngle: Double)