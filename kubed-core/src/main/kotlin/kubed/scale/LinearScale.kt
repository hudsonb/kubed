package kubed.scale

import kubed.array.ticks
import kubed.array.tickStep
import kubed.interpolate.interpolateNumber

open class LinearScale<R>(interpolate: (R, R) -> (Double) -> R,
                          uninterpolate: ((R, R) -> (R) -> Double)? = null,
                          rangeComparator: Comparator<R>? = null) : ContinuousScale<R>(interpolate, uninterpolate, rangeComparator) {
    override fun deinterpolate(a: Double, b: Double): (Double) -> Double {
        val b2 = b - a
        return when(b2) {
            -0.0, +0.0, Double.NaN -> { _ -> b2 }
            else -> { x -> (x - a) / b2 }
        }
    }

    override fun reinterpolate(a: Double, b: Double): (Double) -> Double = interpolateNumber(a, b)

    fun nice(count: Int = 10): LinearScale<R> {
        val i = domain.size - 1
        val n = count
        val start: Double = domain.first()
        val stop: Double = domain.last()
        var step = tickStep(start, stop, n)

        if(step > 0) {
            step = tickStep(Math.floor(start / step) * step, Math.ceil(stop / step) * step, n)
            domain[0] = Math.floor(start / step) * step
            domain[i] = Math.ceil(stop / step) * step
        }

        return this
    }

    override fun ticks(count: Int) = ticks(domain.first(), domain.last(), count)

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
//            val alphabet = charArrayOf('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
//                    'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z');
//            val scale = LinearScale<Char>({ _, _ ->  { t -> alphabet[((alphabet.size - 1) * t).toInt()]} },
//                                          { _, _ -> { c -> (alphabet.indexOf(c) + 1.0) / alphabet.size } })
//            scale.domain(listOf(0.0, 100.0, 1000.0))
//            scale.range(listOf('a', 'z'))
//
//            println("0.0 -> " + scale(0.0))
//            println("100.0 -> " + scale(1000.0))
//            println("a -> " + scale.invert('a'))
//            println("z -> " + scale.invert('z'))
//            println("h -> " + scale.invert('s'))
//            println("28 -> " + scale(scale.invert('s')))

//            val scale: LinearScale<Double> = LinearScale(::numberInterpolator,
//                                    { a, b ->
//                                        val b2 = b - a
//                                        when(b2) {
//                                            -0.0, +0.0, Double.NaN -> constant(b2)
//                                            else -> { x -> (x - a) / b2 }
//                                        }
//                                    },
//                                    java.util.Comparator { a, b -> a.compareTo(b) }).domain(listOf(0.0, 10.0, 1000.0))
//                                                         .range(listOf(0.0, 250.0, 1000.0))
//            println(scale(5.0))
//            println(scale.invert(scale(5.0)))
//            println(scale.nice())
        }
    }
}
