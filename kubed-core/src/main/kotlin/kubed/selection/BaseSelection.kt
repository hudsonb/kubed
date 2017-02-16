package kubed.selection

import javafx.scene.Node
import javafx.scene.paint.Paint

interface BaseSelection<T : BaseSelection<T>> {
    fun opacity(value: Double): T
    fun opacity(value: (d: Any, i: Int, group: List<Node?>) -> Double): T

    fun rotateX(angle: Double): T
    fun rotateX(angle: (d: Any, i: Int, group: List<Node?>) -> Double): T

    fun rotateY(angle: Double): T
    fun rotateY(angle: (d: Any, i: Int, group: List<Node?>) -> Double): T

    fun rotateZ(angle: Double): T
    fun rotateZ(angle: (d: Any, i: Int, group: List<Node?>) -> Double): T
    
    fun scaleX(x: Double): T
    fun scaleX(x: (d: Any, i: Int, group: List<Node?>) -> Double): T

    fun scaleY(y: Double): T
    fun scaleY(y: (d: Any, i: Int, group: List<Node?>) -> Double): T

    fun scaleZ(z: Double): T
    fun scaleZ(z: (d: Any, i: Int, group: List<Node?>) -> Double): T

    fun translateX(x: Double): T
    fun translateX(x: (d: Any, i: Int, group: List<Node?>) -> Double): T

    fun translateY(y: Double): T
    fun translateY(y: (d: Any, i: Int, group: List<Node?>) -> Double): T

    fun translateZ(z: Double): T
    fun translateZ(z: (d: Any, i: Int, group: List<Node?>) -> Double): T

    fun fill(paint: Paint): T
    fun fill(paint: (d: Any, i: Int, group: List<Node?>) -> Paint): T

    fun stroke(paint: Paint): T
    fun stroke(paint: (d: Any, i: Int, group: List<Node?>) -> Paint): T
}
