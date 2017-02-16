package kubed.transition

import javafx.animation.Animation
import javafx.animation.ParallelTransition
import javafx.animation.PauseTransition
import javafx.animation.SequentialTransition
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.util.Duration

class KubedTransition {
    companion object {
        val DEFAULT_DELAY: Duration = Duration.ZERO
    }

    val transition = SequentialTransition()
    val pauseTransition = PauseTransition()
    val parallelTransition = ParallelTransition()

    init {
        transition.children += pauseTransition
        transition.children += parallelTransition
    }

    val delayProperty: ObjectProperty<Duration>
        get() = pauseTransition.delayProperty()
    fun delayProperty() = delayProperty
    var delay: Duration
        get() = delayProperty.get()
        set(value) = delayProperty.set(value)

    val durationProperty: ObjectProperty<Duration> by lazy { SimpleObjectProperty<Duration>(DEFAULT_DURATION) }
    fun durationProperty() = durationProperty
    var duration: Duration
        get() = durationProperty.get()
        set(value) = durationProperty.set(value)

    val statusProperty = parallelTransition.statusProperty()
    fun statusProperty() = statusProperty
    private val status: Animation.Status
        get() = statusProperty.get()
}
