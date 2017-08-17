package kubed.demo

import javafx.application.Application
import javafx.scene.Cursor
import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.stage.Stage
import kubed.scale.scaleCategory10
import kubed.selection.Selection
import kubed.util.isTruthy

import kubed.selection.selectAll
import kubed.shape.circle


class ApolloniusDemo: Application() {
    var outerRings: Selection<() -> Data?>? = null
    var innerRings: Selection<() -> Data?>? = null

    override fun start(primaryStage: Stage?) {
        val width = 960.0
        val height = 500.0

        val root = Group()
        root.prefWidth(width)
        root.prefHeight(height)

        val c1 = Circle(380.0, 250.0, 80.0, Color(.25, .25, .25, 0.5))
        c1.cursor = Cursor.HAND
        addDragAndDrop(c1)

        val c2 = Circle(600.0, 100.0, 20.0, Color(.25, .25,.25, 0.5))
        c2.cursor = Cursor.HAND
        addDragAndDrop(c2)
        
        val c3 = Circle(600.0, 300.0, 120.0, Color(.25, .25, .25, 0.5))
        c3.cursor = Cursor.HAND
        addDragAndDrop(c3)

        val color = scaleCategory10<Int>()

        val outerRing = circle<() -> Data?> {
            centerX { f, _ -> f()?.x ?: 0.0 }
            centerY { f, _ -> f()?.y ?: 0.0 }
            radius { f, _ -> f()?.r ?: 0.0 }
            fill(null)
            stroke { _, i -> color(i) }
            styleClasses(".ring")
         }

        val innerRing = circle<() -> Data?> {
            centerX { f, _ -> f()?.x ?: 0.0 }
            centerY { f, _ -> f()?.y ?: 0.0 }
            radius { f, _ -> f()?.r?.minus(3) ?: 0.0 }
            fill(null)
            stroke { _, i -> color(i).deriveColor(1.0, 1.0, 1.0, .5)}
            strokeWidth(5.0)
            styleClasses(".ring")
        }

        val rings = root.selectAll<() -> Data?>()
                .data(listOf({ apolloniusCircle(c1.centerX, c1.centerY, +c1.radius, c2.centerX, c2.centerY, +c2.radius, c3.centerX, c3.centerY, +c3.radius) },
                             { apolloniusCircle(c1.centerX, c1.centerY, +c1.radius, c2.centerX, c2.centerY, +c2.radius, c3.centerX, c3.centerY, -c3.radius) },
                             { apolloniusCircle(c1.centerX, c1.centerY, +c1.radius, c2.centerX, c2.centerY, -c2.radius, c3.centerX, c3.centerY, +c3.radius) },
                             { apolloniusCircle(c1.centerX, c1.centerY, +c1.radius, c2.centerX, c2.centerY, -c2.radius, c3.centerX, c3.centerY, -c3.radius) },
                             { apolloniusCircle(c1.centerX, c1.centerY, -c1.radius, c2.centerX, c2.centerY, +c2.radius, c3.centerX, c3.centerY, +c3.radius) },
                             { apolloniusCircle(c1.centerX, c1.centerY, -c1.radius, c2.centerX, c2.centerY, +c2.radius, c3.centerX, c3.centerY, -c3.radius) },
                             { apolloniusCircle(c1.centerX, c1.centerY, -c1.radius, c2.centerX, c2.centerY, -c2.radius, c3.centerX, c3.centerY, +c3.radius) },
                             { apolloniusCircle(c1.centerX, c1.centerY, -c1.radius, c2.centerX, c2.centerY, -c2.radius, c3.centerX, c3.centerY, -c3.radius) }))
                .enter()
        innerRings = rings.append { f, i, _ -> innerRing(f, i) }
        outerRings = rings.append { f, i, _ -> outerRing(f, i) }

        root.children += c1
        root.children += c2
        root.children += c3

        update(innerRings, outerRings)

        val scene = Scene(root)
        primaryStage?.scene = scene
        primaryStage?.show()
    }

    fun addDragAndDrop(node: Node) {
        data class Context(var x: Double, var y: Double)

        val context = Context(0.0, 0.0)
        node.setOnMousePressed {
            node as Circle
            context.x = it.sceneX//node.centerX
            context.y = it.sceneY//node.centerY
        }
        node.setOnMouseDragged {
            node as Circle
            node.centerX += it.sceneX - context.x
            node.centerY += it.sceneY - context.y
            context.x = it.sceneX
            context.y = it.sceneY

            update(innerRings, outerRings)
        }
    }

    fun update(innerRings: Selection<() -> Data?>?, outerRings: Selection<() -> Data?>?) {
        innerRings?.forEach<Circle> { f, _, _ ->
            val c = f()
            if(c != null) {
                isVisible = true
                centerX = c.x
                centerY = c.y
                radius = c.r - 3
            }
            else isVisible = false
        }

        outerRings?.forEach<Circle> { f, _, _ ->
            val c = f()
            if(c != null) {
                isVisible = true
                centerX = c.x
                centerY = c.y
                radius = c.r
            }
            else isVisible = false
        }
    }

    fun apolloniusCircle(x1: Double, y1: Double, r1: Double, x2: Double, y2: Double, r2: Double, x3: Double, y3: Double, r3: Double): Data? {
        val a2 = 2 * (x1 - x2)
        val b2 = 2 * (y1 - y2)
        val c2 = 2 * (r2 - r1)
        val d2 = x1 * x1 + y1 * y1 - r1 * r1 - x2 * x2 - y2 * y2 + r2 * r2
        val a3 = 2 * (x1 - x3)
        val b3 = 2 * (y1 - y3)
        val c3 = 2 * (r3 - r1)
        val d3 = x1 * x1 + y1 * y1 - r1 * r1 - x3 * x3 - y3 * y3 + r3 * r3

        val ab = a3 * b2 - a2 * b3
        val xa = (b2 * d3 - b3 * d2) / ab - x1
        val xb = (b3 * c2 - b2 * c3) / ab

        val ya = (a3 * d2 - a2 * d3) / ab - y1
        val yb = (a2 * c3 - a3 * c2) / ab

        val A = xb * xb + yb * yb - 1
        val B = 2 * (xa * xb + ya * yb + r1)
        val C = xa * xa + ya * ya - r1 * r1
        val r = if(A.isTruthy()) (-B - Math.sqrt(B * B - 4 * A * C)) / (2 * A) else (-C / B)
        return if(r.isNaN()) null else Data(xa + xb * r + x1, ya + yb * r + y1, Math.abs(r))
    }

    data class Data(val x: Double, val y: Double, val r: Double)

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            launch(ApolloniusDemo::class.java, *args)
        }
    }
}