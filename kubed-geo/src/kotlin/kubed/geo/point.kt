package kubed.geo

import kubed.math.EPSILON
import kotlin.math.abs

fun pointsEqual(a: DoubleArray, b: DoubleArray) = abs(a[0] - b[0]) < EPSILON && abs(a[1] - b[1]) < EPSILON