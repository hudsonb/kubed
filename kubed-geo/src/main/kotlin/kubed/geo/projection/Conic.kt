package kubed.geo.projection

import kubed.math.toDegrees
import kubed.math.toRadians
import kotlin.math.PI

fun conicProjection(factory: ConicProjectorFactory, init: ConicProjection.() -> Unit) = ConicProjection(factory).apply(init)

abstract class ConicProjectorFactory : ProjectorFactory {
    var phi0 = 0.0
    var phi1 = PI / 3
}

class ConicProjection(factory: ConicProjectorFactory) : Projection(factory) {
    init {
        factory.phi0 = 0.0
        factory.phi1 = PI / 3
        project = factory.create()
    }

    var parallels: DoubleArray
        get() {
            factory as ConicProjectorFactory
            return doubleArrayOf(factory.phi0.toDegrees(), factory.phi1.toDegrees())
        }

        set(value) {
            factory as ConicProjectorFactory
            factory.phi0 = value[0].toRadians()
            factory.phi1 = value[1].toRadians()
            project = factory.create()
        }
}

