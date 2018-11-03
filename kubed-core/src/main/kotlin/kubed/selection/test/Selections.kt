package kubed.selection

import javafx.scene.Node
import javafx.scene.paint.Paint

class Selection2
{
    fun selectAll(selector: String, block: Selection2.() -> Unit)
    {
    }

    fun select(selector: String, block: Selection2.() -> Unit)
    {
    }


    fun append(node: () -> Node)
    {
    }

    fun data(data: List<*>, block: JoinedSelection.() -> Unit)
    {
    }
}

class JoinedSelection
{
    fun append(node: (datum: Any, index: Int, nodes: List<Node?>) -> Node)
    {
    }
}

interface EnterSelection
{
}

interface ExitSelection
{

}

///**
// * Applies the given opacity to each node in the selection.
// *
// * @param value The opacity value to be applied.
// */
//fun opacity(value: Double)
//
///**
// * Applies a calculated opacity to each node in the selection.
// *
// * @param value The opacity calculate function.
// */
//fun opacity(value: (d: T, i: Int, group: List<Node?>) -> Double)
//
//fun rotateX(angle: Double)
//fun rotateX(angle: (d: T, i: Int, group: List<Node?>) -> Double)
//
//fun rotateY(angle: Double): S
//fun rotateY(angle: (d: T, i: Int, group: List<Node?>) -> Double)
//
//fun rotateZ(angle: Double)
//fun rotateZ(angle: (d: T, i: Int, group: List<Node?>) -> Double)
//
//fun scaleX(x: Double)
//fun scaleX(x: (d: T, i: Int, group: List<Node?>) -> Double)
//
//fun scaleY(y: Double)
//fun scaleY(y: (d: T, i: Int, group: List<Node?>) -> Double)
//
//fun scaleZ(z: Double)
//fun scaleZ(z: (d: T, i: Int, group: List<Node?>) -> Double)
//
//fun translateX(x: Double)
//fun translateX(x: (d: T, i: Int, group: List<Node?>) -> Double)
//
//fun translateY(y: Double): S
//fun translateY(y: (d: T, i: Int, group: List<Node?>) -> Double)
//
//fun translateZ(z: Double): S
//fun translateZ(z: (d: T, i: Int, group: List<Node?>) -> Double)
//
//fun fill(paint: Paint?)
//fun fill(paint: (d: T, i: Int, group: List<Node?>) -> Paint?)
//
//fun stroke(paint: Paint?)
//fun stroke(paint: (d: T, i: Int, group: List<Node?>) -> Paint?)