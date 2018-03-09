package kubed.geo.projection

import javafx.beans.property.SimpleObjectProperty
import kotlin.math.PI

abstract class ConicProjection(projector: Projector) : MutableProjection(projector) {
    val parallelsProperty = SimpleObjectProperty<DoubleArray>(doubleArrayOf(0.0, PI / 3))
    var parallels: DoubleArray
        get() = parallelsProperty.get()
        set(values) = parallelsProperty.set(values)
}

