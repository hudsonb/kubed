package kubed.geo.projection

import javafx.beans.property.SimpleDoubleProperty

class Parallel1Projection(factory: (Double) -> Projector) : MutableProjection(factory(0.0)) {
    val parallelProperty = SimpleDoubleProperty(0.0)
    var parallel: Double
        get() = parallelProperty.get()
        set(value) = parallelProperty.set(value)

    init {
        parallelProperty.addListener { _ ->
            projector = factory(parallel)
            invalidate()
        }
    }
}