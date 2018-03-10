package kubed.geo.projection

import javafx.beans.property.SimpleObjectProperty
import kubed.math.toRadians
import kotlin.math.PI

class ConicProjection(factory: (Double, Double) -> Projector) : MutableProjection(factory(0.0, PI / 3)) {
    val parallelsProperty = SimpleObjectProperty<DoubleArray>(doubleArrayOf(0.0, 60.0))
    var parallels: DoubleArray
        get() = parallelsProperty.get()
        set(values) = parallelsProperty.set(values)

    init {
        parallelsProperty.addListener { _ ->
            val phi0 = parallels[0].toRadians()
            val phi1 = parallels[1].toRadians()

            projector = factory(phi0, phi1)
        }
    }
}

