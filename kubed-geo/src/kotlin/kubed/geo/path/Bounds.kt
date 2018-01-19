package kubed.geo.path

import kubed.geo.GeometryStream

class Bounds : GeometryStream {
    private var x0 = Double.POSITIVE_INFINITY
    private var y0 = x0
    private var x1 = -x0
    private var y1 = x1

    fun result(): Array<DoubleArray> {
        val bounds = arrayOf(doubleArrayOf(x0, y0), doubleArrayOf(x1, y1))

        x0 = Double.POSITIVE_INFINITY
        y0 = x0
        x1 = -x0
        y1 = x1

        return bounds
    }

    override fun point(x: Double, y: Double, z: Double) {
        if(x < x0) x0 = x
        if(x > x1) x1 = x
        if(y < y0) y0 = y
        if(y > y1) y1 = y
    }
}