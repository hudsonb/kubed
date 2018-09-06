package kubed.animation

import javafx.animation.Animation
import javafx.animation.Transition
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.shape.Path
import javafx.util.Duration
import kubed.shape.totalLength
import kubed.transition.DEFAULT_DURATION

class DrawPathTransition(val path: Path) : Transition() {
    private val length = path.totalLength
    private val stroked = path.strokeDashArray.isNotEmpty()
    private val dashArray: List<Double> = if(stroked) ArrayList(path.strokeDashArray) else emptyList()
    private val dashSum = dashArray.sum()
    private val dashOffset = path.strokeDashOffset

    var duration: Duration
        get() = durationProperty.get()
        set(value) {
            durationProperty.set(value)
        }
    val durationProperty = SimpleObjectProperty(DEFAULT_DURATION)

    init {
        durationProperty.addListener({ _ -> cycleDuration = duration })

        if(stroked) {
            statusProperty().addListener({ _, _, status ->
                if(status == Animation.Status.STOPPED) {
                    path.strokeDashOffset = dashOffset
                    path.strokeDashArray.setAll(dashArray)
                }
            })
        }
    }

    override fun interpolate(frac: Double) {
        val l = length * frac
        if(stroked) {
            path.strokeDashOffset = l

            val n = (l / dashSum).toInt()
            path.strokeDashArray.clear()
            path.strokeDashArray.addAll(0.0, l)
            (1..n).forEach { path.strokeDashArray.addAll(dashArray) }
            path.strokeDashArray.addAll(0.0, length)
        }
        else path.strokeDashOffset = length - l
    }
}