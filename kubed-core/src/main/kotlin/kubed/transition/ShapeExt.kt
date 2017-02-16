package kubed.transition

import javafx.animation.FillTransition
import javafx.animation.Interpolator
import javafx.animation.StrokeTransition
import javafx.scene.paint.Color
import javafx.scene.shape.Shape
import javafx.util.Duration

/**
 * Creates a [FillTransition] on this [Shape] with the provided parameters.
 *
 * The fill begins at the shape's current [Shape.fillProperty].
 *
 * @see FillTransition
 */
fun Shape.fillTo(to: Color,
                 duration: Duration = DEFAULT_DURATION,
                 delay: Duration = Duration.ZERO,
                 from: Color? = null,
                 interpolator: Interpolator? = null,
                 cycleCount: Int = 1,
                 autoReverse: Boolean = false): FillTransition {
    val ft = FillTransition(duration, this)
    ft.fromValue = from
    ft.toValue = to
    ft.duration = duration
    ft.delay = delay
    ft.interpolator = interpolator ?: ft.interpolator
    ft.cycleCount = cycleCount
    ft.isAutoReverse = autoReverse

    return ft
}

/**
 * Creates a [StrokeTransition] on this [Shape] with the provided parameters.
 *
 * The fill begins at the shape's current [Shape.strokeProperty].
 *
 * @see StrokeTransition
 */
fun Shape.strokeTo(to: Color,
                   duration: Duration = DEFAULT_DURATION,
                   delay: Duration = Duration.ZERO,
                   from: Color? = null,
                   interpolator: Interpolator? = null,
                   cycleCount: Int = 1,
                   autoReverse: Boolean = false): StrokeTransition {
    val st = StrokeTransition(duration, this)
    st.fromValue = from
    st.toValue = to
    st.duration = duration
    st.delay = delay
    st.interpolator = interpolator ?: st.interpolator
    st.cycleCount = cycleCount
    st.isAutoReverse = autoReverse

    return st
}
