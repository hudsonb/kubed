package kubed.demo

import javafx.application.Application
import javafx.stage.Stage
import kubed.ease.CubicInInterpolator
import kubed.selection.Selection
import kubed.selection.selectAll
import kubed.shape.rect
import javafx.scene.*
import kubed.interpolate.color.*
import kubed.scale.*
import kubed.timer.timer
import java.lang.Math.random
import kotlin.math.*


class ManyPointDemo : Application() {
    override fun start(primaryStage: Stage?) {
        val root = Group()

        val width = 600.0
        val height = 600.0

        val numPoints = 7000
        val pointWidth = 4.0
        val pointMargin = 3.0

        val points = createPoints(numPoints)
        val toGrid = { p: List<Point> -> gridLayout(p, pointWidth + pointMargin, width) }
        val toSine = { p: List<Point> -> sineLayout(p, pointWidth + pointMargin, width, height) }
        val toSpiral = { p: List<Point> -> spiralLayout(p, pointWidth + pointMargin, width, height) }
        val toPhyllotaxis = { p: List<Point> -> phyllotaxisLayout(p, pointWidth + pointMargin, width / 2, height / 2) }
        val layouts = listOf(toSine, toPhyllotaxis, toSpiral, toPhyllotaxis, toGrid)

        toGrid(points)
        //toSine(points)
        //toPhyllotaxis(points)
        //toSpiral(points)

        val color = scaleSequential(interpolateYlOrBr()).domain(points.size - 1.0, 0.0)
        val c = rect<Point> {
            translateX { d, _ -> d.x }
            translateY { d, _ -> d.y }
            width(pointWidth)
            height(pointWidth)
            fill { _, i -> color(i.toDouble()) }
            stroke(null)
            cacheHint(CacheHint.SPEED)
        }

        val square = root.selectAll<Point>("Circle")
                .data(points)
                .enter()
                .append { d, i, _ -> c(d, i) }

        animate(square, points, layouts, 0)

        val scene = Scene(root)
        primaryStage?.width = width
        primaryStage?.height = height

        primaryStage?.scene = scene
        primaryStage?.show()
    }

    fun animate(squares: Selection<Point>, points: List<Point>, layouts: List<(List<Point>) -> Unit>, currentLayout: Int) {
        for(p in points) {
            p.sx = p.x
            p.sy = p.y
        }

        val layout = layouts[currentLayout]
        layout(points)

        for(p in points) {
            p.tx = p.x
            p.ty = p.y
        }

        val interpolator = CubicInInterpolator()
        timer { elapsed ->
            val t = min(1.0, elapsed / 1500.0)

            // Update the point positions
            squares.translateX { d, _, _ -> interpolator.interpolate(d.sx, d.tx, t) }
                   .translateY { d, _, _ -> interpolator.interpolate(d.sy, d.ty, t) }

            if(t >= 1.0) {
               stop()
               animate(squares, points, layouts, (currentLayout + 1) % layouts.size)
            }
        }
    }

    fun createPoints(n: Int): List<Point> {
        val points = ArrayList<Point>(n)
        for(i in 0 until n) points += Point(0.0, 0.0)
        return points
    }

    fun phyllotaxisLayout(points: List<Point>, pointWidth: Double, xOffset: Double = 0.0, yOffset: Double = 0.0, iOffset: Int = 0) {
        val theta = PI * (3 - sqrt(5.0))
        val pointRadius = pointWidth / 2

        for(i in points.indices) {
            val point = points[i]
            val index = (i + iOffset) % points.size
            val si = sqrt(index.toDouble())
            val it = index * theta
            val phylloX = pointRadius * si * cos(it)
            val phylloY = pointRadius * si * sin(it)

            point.x = xOffset + phylloX - pointRadius
            point.y = yOffset + phylloY - pointRadius
        }
    }

    fun gridLayout(points: List<Point>, pointWidth: Double, gridWidth: Double) {
        val pointHeight = pointWidth
        val pointsPerRow = floor(gridWidth / pointWidth)

        var point: Point
        for(i in points.indices) {
            point = points[i]
            point.x = pointWidth * (i % pointsPerRow)
            point.y = pointHeight * floor(i / pointsPerRow)
        }
    }

    fun randomLayout(points: List<Point>, pointWidth: Double, width: Double, height: Double) {
        for(i in points.indices) {
            val point = points[i]
            point.x = random() * (width - pointWidth)
            point.y = random() * (height - pointWidth)
        }
    }

    fun sineLayout(points: List<Point>, pointWidth: Double, width: Double, height: Double) {
        val amplitude = 0.3 * (height / 2)
        val yOffset = height / 2
        val periods = 3
        val yScale = scaleLinear<Double> {
            domain(0.0, (points.size - 1).toDouble())
            range(0.0, periods * 2 * PI)
            nice(10)
        }

        for(i in points.indices) {
            val point = points[i]
            point.x = (i / points.size.toDouble()) * (width - pointWidth)
            point.y = amplitude * sin(yScale(i.toDouble())) + yOffset
        }
    }

    fun spiralLayout(points: List<Point>, pointWidth: Double, width: Double, height: Double) {
        val xOffset = width / 2
        val yOffset = height / 2
        val periods = 20

        val rScale = scaleLinear<Double> {
            domain(0.0, points.size - 1.0)
            range(0.0, min(width / 2, height / 2) - pointWidth)
        }

        val thetaScale = scaleLinear<Double> {
            domain(0.0, points.size - 1.0)
            range(0.0, periods * 2 * PI)
        }

        for(i in points.indices) {
            val point = points[i]
            point.x = rScale(i.toDouble()) * cos(thetaScale(i.toDouble())) + xOffset
            point.y = rScale(i.toDouble()) * sin(thetaScale(i.toDouble())) + yOffset
        }
    }

    data class Point(var x: Double, var y: Double, var sx: Double = x, var sy: Double = y, var tx: Double = x, var ty: Double = y)

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            launch(ManyPointDemo::class.java, *args)
        }
    }
}

