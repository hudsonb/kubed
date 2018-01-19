package kubed.format

import kubed.math.toExponential
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

data class Result(val coefficient: String, val exponent: Int)

fun formatDecimal(value: Double, precision: Int = 20): Result {
    val exp = value.toExponential(precision)

    val i = exp.indexOf("E")
    if(i < 0) throw IllegalArgumentException() // NaN, ±Infinity
    val coefficient = exp.substring(0, i)

    // The string returned by toExponential either has the form \d\.\d+e[-+]\d+
    // (e.g., 1.2e+3) or the form \de[-+]\d+ (e.g., 1e+3).
    if(coefficient.length > 1)
        coefficient.get(0) + coefficient.substring(2)

    return Result(coefficient, exp.toInt())
}

fun formatPrefixAuto(x: Double, p: Int): String {
    val (coefficient, exponent) = formatDecimal(x, p)
    val i = (exponent - (max(-8.0, min(8.0, floor(exponent / 3.0))) * 3) + 1).toInt()
    val n = coefficient.length
    if(i == n)
        return coefficient

    var result = coefficient
    if(i == n)
        result = coefficient
    if(i > n) {
       for(j in 0..i - n + 1) {
           result += '0'
       }
    }
    else if(i > 0)
        result = coefficient.substring(0, i) + "." + coefficient.substring(i)
    else {
        result = "0."
        (0..1 - i).forEach { result += '0' }
        result += formatDecimal(x, Math.max(0, p + i - 1)).coefficient // less than 1y!
    }

    return result
}

fun formatRounded(x: Double, p: Int): String {
    val (coefficient, exponent) = formatDecimal(x, p)

    var result : String
    when {
        exponent < 0 -> {
            result = "0."
            (0..exponent).forEach { result += '0' }
            result += coefficient
        }

        coefficient.length > exponent + 1 ->
            result = coefficient.substring(0, exponent + 1) + "." + coefficient.substring(exponent + 1)

        else -> {
            result = coefficient
            (0..exponent - coefficient.length + 2).forEach { result += '0' }
        }
    }

    return result
}