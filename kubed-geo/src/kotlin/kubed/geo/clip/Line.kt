package kubed.geo.clip

import kubed.util.isFalsy

fun clipLine(a: DoubleArray, b: DoubleArray, x0: Double, y0: Double, x1: Double, y1: Double): Boolean {
    val ax = a[0]
    val ay = a[1]
    val bx = b[0]
    val by = b[1]
    var t0 = 0.0
    var t1 = 1.0
    val dx = bx - ax
    val dy = by - ay
    var r = x0 - ax

    if(dx.isFalsy() && r < 0) return false
    r /= dx
    if(dx < 0) {
        if(r < t0) return false
        if(r < t1) t1 = r
    }
    else if(dx > 0) {
        if(r > t1) return false
        if(r > t0) t0 = r
    }

    r = x1 - ax
    if(dx.isFalsy() && r < 0) return false
    r /= dx
    if(dx < 0) {
        if(r > t1) return false
        if(r > t0) t0 = r
    }
    else if(dx > 0) {
        if(r < t0) return false
        if(r < t1) t1 = r
    }

    r = y0 - ay
    if(dy.isFalsy() && r > 0) return false
    r /= dy
    if(dy < 0) {
        if(r < t0) return false
        if(r < t1) t1 = r
    }
    else if(dy > 0) {
        if(r > t1) return false
        if(r > t0) t0 = r
    }

    r = y1 - ay
    if(dy.isFalsy() && r < 0) return false
    r /= dy
    if(dy < 0) {
        if(r > t1) return false
        if(r > t0) t0 = r
    }
    else if(dy > 0) {
        if(r < t0) return false
        if(r < t1) t1 = r
    }

    if(t0 > 0) {
        a[0] = ax + t0 * dx
        a[1] = ay + t0 * dy
    }

    if(t1 < 1) {
        b[0] = ax + t1 * dx
        b[1] = ay + t1 * dy
    }

    return true
}