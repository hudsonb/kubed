package kubed.path

import javafx.scene.canvas.GraphicsContext
import javafx.scene.shape.Shape

interface Context {
    fun moveTo(x: Double, y: Double): Context
    fun lineTo(x: Double, y: Double): Context
    fun quadraticCurveTo(controlX: Double, controlY: Double, x: Double, y: Double): Context
    fun bezierCurveTo(controlX: Double, controlY: Double, controlX2: Double, controlY2: Double, x: Double, y: Double): Context
    fun arcTo(x1: Double, y1: Double, x2: Double, y2: Double, radius: Double): Context
    fun arc(x: Double, y: Double, r: Double, a0: Double, a1: Double, ccw: Boolean): Context
    fun rect(x: Double, y: Double, w: Double, h: Double): Context
    fun closePath(): Context
    operator fun invoke(): Shape
    operator fun invoke(gc: GraphicsContext)
}
