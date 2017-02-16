package kubed.transition

import javafx.animation.*
import javafx.geometry.Point3D
import javafx.scene.Node
import javafx.scene.shape.Path
import javafx.scene.shape.Shape
import javafx.scene.transform.Rotate
import javafx.util.Duration
import kubed.selection.Group
import kubed.selection.Selection

/**
 * Creates a [FadeTransition] on this [Node] with the provided parameters.
 *
 * The [FadeTransition] fades from the node's current opacity.
 *
 * @see FadeTransition
 */
fun Node.fadeTo(to: Double,
                duration: Duration = DEFAULT_DURATION,
                delay: Duration = Duration.ZERO,
                from: Double = Double.NaN,
                by: Double = 0.0,
                interpolator: Interpolator? = null,
                cycleCount: Int = 1,
                autoReverse: Boolean = false,
                play: Boolean = true): FadeTransition {
    val ft = FadeTransition(duration, this)
    ft.delay = delay
    ft.duration = duration
    ft.isAutoReverse = autoReverse
    ft.cycleCount = cycleCount
    ft.fromValue = from
    ft.toValue = to
    ft.byValue = by
    ft.interpolator = interpolator ?: ft.interpolator

    if(play) ft.play()

    return ft
}

/**
 * Creates a [RotateTransition] on this [Node] about the X axis with the provided parameters.
 *
 * The rotation rotates from the node's current angle.
 *
 * @see RotateTransition
 */
fun Node.rotateXTo(to: Double,
                   duration: Duration = Duration.millis(250.0),
                   delay: Duration = Duration.ZERO,
                   from: Double = Double.NaN,
                   by: Double = 0.0,
                   interpolator: Interpolator? = null,
                   cycleCount: Int = 1,
                   autoReverse: Boolean = false,
                   play: Boolean = true) =
        rotateTo(to, duration, delay, from, by, Rotate.X_AXIS, interpolator, cycleCount, autoReverse, play)

/**
 * Creates a [RotateTransition] on this [Node] about the Y axis with the provided parameters.
 *
 * The rotation rotates from the node's current angle.
 *
 * @see RotateTransition
 */
fun Node.rotateYTo(to: Double,
                   duration: Duration = Duration.millis(250.0),
                   delay: Duration = Duration.ZERO,
                   from: Double = Double.NaN,
                   by: Double = 0.0,
                   interpolator: Interpolator? = null,
                   cycleCount: Int = 1,
                   autoReverse: Boolean = false,
                   play: Boolean = true) =
        rotateTo(to, duration, delay, from, by, Rotate.Y_AXIS, interpolator, cycleCount, autoReverse, play)

/**
 * Creates a [RotateTransition] on this [Node] about the Z axis with the provided parameters.
 *
 * The rotation rotates from the node's current angle.
 *
 * @see RotateTransition
 */
fun Node.rotateZTo(to: Double,
                   duration: Duration = Duration.millis(250.0),
                   delay: Duration = Duration.ZERO,
                   from: Double = Double.NaN,
                   by: Double = 0.0,
                   interpolator: Interpolator? = null,
                   cycleCount: Int = 1,
                   autoReverse: Boolean = false,
                   play: Boolean = true) =
        rotateTo(to, duration, delay, from, by, Rotate.Z_AXIS, interpolator, cycleCount, autoReverse, play)


/**
 * Creates a [RotateTransition] on this [Node] with the provided parameters.
 *
 * The [RotateTransition] rotates about the node's [Node.rotationAxisProperty] unless [axis] is provided.
 *
 * The rotation rotates from the node's current angle.
 *
 * @see RotateTransition
 */
fun Node.rotateTo(to: Double,
                  duration: Duration = Duration.millis(250.0),
                  delay: Duration = Duration.ZERO,
                  from: Double = Double.NaN,
                  by: Double = 0.0,
                  axis: Point3D = rotationAxis,
                  interpolator: Interpolator? = null,
                  cycleCount: Int = 1,
                  autoReverse: Boolean = false,
                  play: Boolean = true): RotateTransition {
    val rt = RotateTransition(duration, this)
    rt.axis = axis
    rt.fromAngle = from
    rt.toAngle = to
    rt.byAngle = by
    rt.cycleCount = cycleCount
    rt.delay = delay
    rt.isAutoReverse = autoReverse
    rt.interpolator = interpolator ?: rt.interpolator

    if(play) rt.play()

    return rt
}

/**
 * Creates a X [ScaleTransition] on this [Node] with the provided parameters.
 *
 * The scale begins from the node's current [Node.scaleXProperty].
 *
 * @see ScaleTransition
 * @see scaleTo
 */
fun Node.scaleXTo(to: Double,
                  duration: Duration = Duration.millis(250.0),
                  delay: Duration = Duration.ZERO,
                  from: Double = Double.NaN,
                  by: Double = 0.0,
                  interpolator: Interpolator? = null,
                  cycleCount: Int = 1,
                  autoReverse: Boolean = false,
                  play: Boolean = true) =
        scaleTo(toX = to, fromX = from, byX = by, duration = duration, delay = delay, interpolator = interpolator,
                cycleCount = cycleCount, autoReverse = autoReverse, play = play)

/**
 * Creates a Y [ScaleTransition] on this [Node] with the provided parameters.
 *
 * The scale begins from the node's current [Node.scaleYProperty].
 *
 * @see ScaleTransition
 * @see scaleTo
 */
fun Node.scaleYTo(to: Double,
                  duration: Duration = DEFAULT_DURATION,
                  delay: Duration = Duration.ZERO,
                  from: Double = Double.NaN,
                  by: Double = 0.0,
                  interpolator: Interpolator? = null,
                  cycleCount: Int = 1,
                  autoReverse: Boolean = false,
                  play: Boolean = true) =
        scaleTo(toY = to, fromY = from, byY = by, duration = duration, delay = delay, interpolator = interpolator,
                cycleCount = cycleCount, autoReverse = autoReverse, play = play)

/**
 * Creates a Z [ScaleTransition] on this [Node] with the provided parameters.
 *
 * The scale begins from the node's current [Node.scaleZProperty].
 *
 * @see ScaleTransition
 * @see scaleTo
 */
fun Node.scaleZTo(to: Double,
                  duration: Duration = Duration.millis(250.0),
                  delay: Duration = Duration.ZERO,
                  from: Double = Double.NaN,
                  by: Double = 0.0,
                  interpolator: Interpolator? = null,
                  cycleCount: Int = 1,
                  autoReverse: Boolean = false,
                  play: Boolean = true) =
        scaleTo(toZ = to, fromZ = from, byZ = by, duration = duration, delay = delay, interpolator = interpolator,
                cycleCount = cycleCount, autoReverse = autoReverse, play = play)

/**
 * Creates a [ScaleTransition] on this [Node] with the provided parameters.
 *
 * The scale begins from the node's current [Node.scaleProperty].
 *
 * @see ScaleTransition
 */
fun Node.scaleTo(toX: Double = Double.NaN,
                 fromX: Double = Double.NaN,
                 byX: Double = 0.0,
                 toY: Double = Double.NaN,
                 fromY: Double = Double.NaN,
                 byY: Double = 0.0,
                 toZ: Double = Double.NaN,
                 fromZ: Double = Double.NaN,
                 byZ: Double = 0.0,
                 duration: Duration = Duration.millis(250.0),
                 delay: Duration = Duration.ZERO,
                 interpolator: Interpolator? = null,
                 cycleCount: Int = 1,
                 autoReverse: Boolean = false,
                 play: Boolean = true): ScaleTransition {
    val st = ScaleTransition(duration, this)
    st.fromX = fromX
    st.toX = toX
    st.byX = byX
    st.fromY = fromY
    st.toY = toY
    st.byY = byY
    st.fromZ = fromZ
    st.toZ = toZ
    st.byZ = byZ
    st.delay = delay
    st.interpolator = interpolator ?: st.interpolator
    st.cycleCount = cycleCount
    st.isAutoReverse = autoReverse

    if(play) st.play()

    return st
}

/**
 * Creates a X [TranslateTransition] on this [Node] with the provided parameters.
 *
 * The scale begins from the node's current [Node.translateXProperty].
 *
 * @see TranslateTransition
 * @see translateTo
 */
fun Node.translateXTo(to: Double = Double.NaN,
                      duration: Duration = Duration.millis(250.0),
                      delay: Duration = Duration.ZERO,
                      from: Double = Double.NaN,
                      by: Double = 0.0,
                      interpolator: Interpolator? = null,
                      cycleCount: Int = 1,
                      autoReverse: Boolean = false,
                      play: Boolean = true) =
        translateTo(toX = to, fromX = from, byX = by, duration = duration, delay = delay, interpolator = interpolator,
                    cycleCount = cycleCount, autoReverse = autoReverse, play = play)

/**
 * Creates a Y [TranslateTransition] on this [Node] with the provided parameters.
 *
 * The scale begins from the node's current [Node.translateYProperty].
 *
 * @see TranslateTransition
 * @see translateTo
 */
fun Node.translateYTo(to: Double = Double.NaN,
                      duration: Duration = Duration.millis(250.0),
                      delay: Duration = Duration.ZERO,
                      from: Double = Double.NaN,
                      by: Double = 0.0,
                      interpolator: Interpolator? = null,
                      cycleCount: Int = 1,
                      autoReverse: Boolean = false,
                      play: Boolean = true) =
        translateTo(toY = to, fromY = from, byY = by, duration = duration, delay = delay, interpolator = interpolator,
                    cycleCount = cycleCount, autoReverse = autoReverse, play = play)

/**
 * Creates a Z [TranslateTransition] on this [Node] with the provided parameters.
 *
 * The scale begins from the node's current [Node.translateZProperty].
 *
 * @see TranslateTransition
 * @see translateTo
 */
fun Node.translateZTo(to: Double = Double.NaN,
                      duration: Duration = Duration.millis(250.0),
                      delay: Duration = Duration.ZERO,
                      from: Double = Double.NaN,
                      by: Double = 0.0,
                      interpolator: Interpolator? = null,
                      cycleCount: Int = 1,
                      autoReverse: Boolean = false,
                      play: Boolean = true) =
        translateTo(toZ = to, fromZ = from, byZ = by, duration = duration, delay = delay, interpolator = interpolator,
                    cycleCount = cycleCount, autoReverse = autoReverse, play = play)

/**
 * Creates [TranslateTransition] on this [Node] with the provided parameters.
 *
 * The scale begins from the node's current [Node.translateXProperty], [Node.translateYProperty] and
 * [Node.translateZProperty].
 *
 * @see TranslateTransition
 * @see translateTo
 */
fun Node.translateTo(toX: Double = Double.NaN,
                     fromX: Double = Double.NaN,
                     byX: Double = 0.0,
                     toY: Double = Double.NaN,
                     fromY: Double = Double.NaN,
                     byY: Double = 0.0,
                     toZ: Double = Double.NaN,
                     fromZ: Double = Double.NaN,
                     byZ: Double = 0.0,
                     duration: Duration = Duration.millis(250.0),
                     delay: Duration = Duration.ZERO,
                     interpolator: Interpolator? = null,
                     cycleCount: Int = 1,
                     autoReverse: Boolean = false,
                     play: Boolean = true): TranslateTransition {
    val tt = TranslateTransition(duration, this)
    tt.fromX = fromX
    tt.toX = toX
    tt.byX = byX
    tt.toY = toY
    tt.fromY = fromY
    tt.byY = byY
    tt.fromZ = fromZ
    tt.toZ = toZ
    tt.byZ = byZ
    tt.delay = delay
    tt.interpolator = interpolator ?: tt.interpolator
    tt.cycleCount = cycleCount
    tt.isAutoReverse = autoReverse

    if(play) tt.play()

    return tt
}

fun Node.pathTo(path: Shape,
                duration: Duration = DEFAULT_DURATION,
                delay: Duration = Duration.ZERO,
                orientation: PathTransition.OrientationType = PathTransition.OrientationType.NONE,
                interpolator: Interpolator? = null,
                cycleCount: Int = 1,
                autoReverse: Boolean = false,
                play: Boolean = true): PathTransition
{
    val pt = PathTransition(duration, path, this)
    pt.delay = delay
    pt.orientation = orientation
    pt.interpolator = interpolator ?: pt.interpolator
    pt.cycleCount = cycleCount
    pt.isAutoReverse = autoReverse

    if(play) pt.play()

    return pt
}

fun Node.parallel(vararg animations: Animation,
                  delay: Duration = Duration.ZERO,
                  interpolator: Interpolator? = null,
                  play: Boolean = true): ParallelTransition {
    animations.forEach { it.stop() }
    val pt = ParallelTransition(this, *animations)
    pt.delay = delay
    pt.interpolator = interpolator ?: pt.interpolator

    if(play) pt.play()

    return pt
}

fun Node.sequential(vararg animations: Animation,
                    delay: Duration, interpolator: Interpolator? = null,
                    play: Boolean = true): SequentialTransition {
    animations.forEach { it.stop() }
    val st = SequentialTransition(this, *animations)
    st.delay = delay
    st.interpolator = interpolator ?: st.interpolator

    if(play) st.play()

    return st
}

fun Node.zAngle(): Double {
    val t = localToParentTransform
    return Math.toDegrees(Math.atan2(t.myx, t.mxx))
}

fun Node.xAngle(): Double {
    val t = localToParentTransform
    return Math.toDegrees(Math.atan2(t.mzy, t.mzz))
}

fun Node.yAngle(): Double {
    val t = localToParentTransform
    return Math.toDegrees(Math.atan2(-t.mzx, Math.sqrt(t.mzy * t.mzy + t.mzz * t.mzz)))
}

//internal fun Node.getTransitions(): MutableMap<String, MutableMap<Int, ParallelTransition>>? =
//        properties["__transitions__"] as MutableMap<String, MutableMap<Int, ParallelTransition>>?
//
//internal fun Node.setTransitions(map: Map<String, MutableMap<Int, ParallelTransition>>?) {
//    if(map == null)
//        properties.remove("__transitions__")
//    else
//        properties["__transitions__"] = map
//}

internal fun Node.getTransitions(): MutableMap<String, MutableMap<Int, Transition>>? =
        properties["__transitions__"] as MutableMap<String, MutableMap<Int, Transition>>?

internal fun Node.setTransitions(map: Map<String, MutableMap<Int, Transition>>?) {
    if(map == null)
        properties.remove("__transitions__")
    else
        properties["__transitions__"] = map
}

fun Node.active(name: String = ""): Transition? {
    val outer = getTransitions()
    if(outer != null) {
        val transitions = outer[name]
        if(transitions != null) {
            val t = transitions[transitions.keys.max()]
            if(t != null) {
                val sel = Selection()
                val group = Group(this.parent)
                group += this
                sel += group
                val newTransition = Transition(t, sel, t.name)
                val m = t.metadata[this] ?: TransitionMetadata(this)
                newTransition.metadata[this] = TransitionMetadata(this, m.delay, m.duration, m.cacheHint, m.interpolator)
                return newTransition
            }
        }
    }

    return null
}