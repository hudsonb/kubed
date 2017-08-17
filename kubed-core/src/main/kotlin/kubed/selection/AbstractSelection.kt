package kubed.selection

import javafx.scene.Node
import javafx.scene.paint.Paint

abstract class AbstractSelection<S : AbstractSelection<S, T>, T> {
    abstract fun opacity(value: Double): S
    abstract fun opacity(value: (d: T, i: Int, group: List<Node?>) -> Double): S

    abstract fun rotateX(angle: Double): S
    abstract fun rotateX(angle: (d: T, i: Int, group: List<Node?>) -> Double): S

    abstract fun rotateY(angle: Double): S
    abstract fun rotateY(angle: (d: T, i: Int, group: List<Node?>) -> Double): S

    abstract fun rotateZ(angle: Double): S
    abstract fun rotateZ(angle: (d: T, i: Int, group: List<Node?>) -> Double): S

    abstract fun scaleX(x: Double): S
    abstract fun scaleX(x: (d: T, i: Int, group: List<Node?>) -> Double): S

    abstract fun scaleY(y: Double): S
    abstract fun scaleY(y: (d: T, i: Int, group: List<Node?>) -> Double): S

    abstract fun scaleZ(z: Double): S
    abstract fun scaleZ(z: (d: T, i: Int, group: List<Node?>) -> Double): S

    abstract fun translateX(x: Double): S
    abstract fun translateX(x: (d: T, i: Int, group: List<Node?>) -> Double): S

    abstract fun translateY(y: Double): S
    abstract fun translateY(y: (d: T, i: Int, group: List<Node?>) -> Double): S

    abstract fun translateZ(z: Double): S
    abstract fun translateZ(z: (d: T, i: Int, group: List<Node?>) -> Double): S

    abstract fun fill(paint: Paint): S
    abstract fun fill(paint: (d: T, i: Int, group: List<Node?>) -> Paint): S

    abstract fun stroke(paint: Paint): S
    abstract fun stroke(paint: (d: T, i: Int, group: List<Node?>) -> Paint): S
}
