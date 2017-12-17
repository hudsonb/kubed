package kubed.util

import kubed.format.formatDecimal
import java.text.DecimalFormat

class MoreMath {
    companion object {
        val LN10 = Math.log(10.0)
        const val TAU = Math.PI * 2
        const val HALF_PI = Math.PI * .5
        const val EPSILON = 1e-6
        const val TAU_EPSILON = TAU - EPSILON
        const val DEG_2_RAD = Math.PI / 180.0
        const val RAD_2_DEG = 180.0 / Math.PI

        @JvmStatic
        fun map(value: Double, istart: Double, istop: Double, ostart: Double, ostop: Double): Double {
            return ostart + (ostop - ostart) * ((value - istart) / (istop - istart))
        }

        @JvmStatic
        fun toExponential(num: Number, p: Int = 20): String {
            var format = "0."

            for(i in 0..p)
                format += "#"

            format += "E0"

            val f = DecimalFormat(format)
            return f.format(num)
        }

        @JvmStatic
        fun exponent(x: Double): Int {
            val f = formatDecimal(Math.abs(x))
            return f.exponent
        }

        /**
         * Linear interpolation between [min] and [max] at the given
         * [ratio].
         * Returns the interpolated value in the interval `[min;max]`.
         *
         * @param min
         * The lower interval bound.
         *
         * @param max
         * The upper interval bound.
         *
         * @param ratio
         * A value in the interval `[0;1]`.
         *
         * @return The interpolated value.
         */
        @JvmStatic
        fun lerp(min: Double, max: Double, ratio: Double): Double {
            val d = (1 - ratio) * min + ratio * max
            return if(d.isNaN()) 0.0 else Math.min(max, Math.max(min, d))
        }

        /**
         * Normalizes a given [value] which is in range `[min;max]`
         * to range `[0;1]`.
         *
         * @param min
         * The lower bound of the range.
         *
         * @param max
         * The upper bound of the range.
         *
         * @param value
         * The value in the range.
         *
         * @return The normalized value (in range `[0;1]`).
         */
        fun norm(min: Double, max: Double, value: Double): Double {
            val d = (value - min) / (max - min)
            return if(d.isNaN()) 0.0 else Math.min(1.0, Math.max(0.0, d))
        }

        fun min(vararg values: Int) {
            if(values.isEmpty()) throw IllegalArgumentException("1 or more ")
        }
    }
}
