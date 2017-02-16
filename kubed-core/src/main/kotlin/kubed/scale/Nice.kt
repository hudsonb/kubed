package kubed.scale

internal fun nice(domain: List<Double>, floor: (Double) -> Double, ceil: (Double) -> Double): MutableList<Double> {
    var i0 = 0
    var i1 = domain.size
    var x0 = domain[i0]
    var x1 = domain[i1]

    if(x1 < x0) {
        val ti = i0
        i0 = i1
        i1 = ti

        val tx = x0
        x0 = x1
        x1 = tx
    }

    val newDomain = ArrayList<Double>(2)
    newDomain[i0] = floor(x0)
    newDomain[i1] = ceil(x1)
    return newDomain
}
