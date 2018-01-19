package kubed.geo.projection

import kubed.geo.GeometryStream

typealias Transformer = (GeometryStream) -> GeometryStream

interface Transform {
    operator fun invoke(lambda: Double, phi: Double): DoubleArray
}

interface Invertable {
    fun invert(x: Double, y: Double): DoubleArray
}

interface InvertableTransform : Transform, Invertable

fun compose(a: Transform, b: Transform): Transform {
    if(a is Invertable && b is Invertable) {
        return object : InvertableTransform {
            override fun invoke(lambda: Double, phi: Double): DoubleArray {
                val p = a(lambda, phi)
                return b(p[0], p[1])
            }

            override fun invert(lambda: Double, phi: Double): DoubleArray {
                val p = b.invert(lambda, phi)
                return a.invert(p[0], p[1])
            }
        }
    }
    else {
        return object : Transform {
            override fun invoke(lambda: Double, phi: Double): DoubleArray {
                val p = a(lambda, phi)
                return b(p[0], p[1])
            }
        }
    }
}