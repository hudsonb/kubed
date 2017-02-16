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
    }
}
