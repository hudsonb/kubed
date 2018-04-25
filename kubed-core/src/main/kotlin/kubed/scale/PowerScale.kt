package kubed.scale

import kubed.interpolate.Deinterpolator
import kubed.interpolate.Reinterpolator
import kubed.interpolate.interpolateNumber

open class PowerScale<R>(val exponent: Double, interpolate: (R, R) -> (Double) -> R) : ContinuousScale<R>(interpolate) {
    override fun deinterpolatorOf(a: Double, b: Double): Deinterpolator<Double> {
        val da = raise(a, exponent)
        val db = raise(b, exponent) - da

        return when {
            db == -0.0 || db == +0.0 || db.isNaN() -> { _ -> db }
            else -> { x -> (raise(x, exponent) - da) / db }
        }
    }

    override fun reinterpolatorOf(a: Double, b: Double): Reinterpolator<Double> {
        val ra = raise(a, exponent)
        val rb = raise(b, exponent) - ra
        return { t -> raise(ra + rb * t, 1.0 / exponent) }
    }

    override fun ticks(count: Int): List<Double> = listOf()

    private fun raise(x: Double, exponent: Double): Double = when {
        x < 0.0 -> -Math.pow(-x, exponent)
        else -> Math.pow(x, exponent)
    }

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            val scale = PowerScale(0.5, ::interpolateNumber)
            scale.domain(listOf(0.0, 52.0))
            scale.range(listOf(20.0, 100.0))

            println("6.0 -> " + scale(6.0) + "\t47.175")
            println("12.0 -> " + scale(12.0))
            println("19.0 -> " + scale(19.0))
            println("23.0 -> " + scale(23.0))
            println("47.0 -> " + scale(47.0))
            println("52.0 -> " + scale(52.0))
        }
    }
}

