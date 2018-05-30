package kubed.demo

import javafx.application.Application
import javafx.geometry.Point2D
import javafx.geometry.Rectangle2D
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.stage.Stage
import kubed.delaunay.Delaunay
import kubed.path.PathContext
import kubed.selection.selectAll
import kubed.shape.circle
import kubed.util.PoissonDiscSampler

/**
 * Demonstrates Poisson-desc sampling.
 *
 * Based on https://beta.observablehq.com/@mbostock/the-delaunays-dual
 */
class DelaunayDemo : Application() {
    override fun start(primaryStage: Stage?) {
        val margin = 40.0
        val outerWidth = 800.0
        val outerHeight = 400.0
        val innerWidth = outerWidth - margin * 2
        val innerHeight = outerHeight - margin * 2

        val root = Group()
        root.translateX = margin
        root.translateY = margin

        val sampler = PoissonDiscSampler(innerWidth, innerHeight, 20.0)

        val points = ArrayList<Point2D>()
        var p: Point2D?
        while(true)
        {
            p = sampler()
            if(p == null) break
            points += p
        }

        val blackDot = circle<Point2D> {
            translateX { p, _ -> p.x }
            translateY { p, _ -> p.y }
            radius { _, _ -> 3.0 }
            fill(Color.BLACK)
        }

        root.selectAll<Point2D>()
            .data(points)
            .enter()
            .append { p, _, _ -> blackDot(p)}


        val delaunay = Delaunay(points, { p -> p.x }, { p -> p.y })

        val context = PathContext()
        delaunay.render(context)
        val path = context()
        path.fill = null
        path.stroke = Color.LIGHTGRAY
        root.children.add(0, path)

        val scene = Scene(root)
        primaryStage?.width = outerWidth
        primaryStage?.height = outerHeight

        primaryStage?.scene = scene
        primaryStage?.show()
    }

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            launch(DelaunayDemo::class.java, *args)
        }
    }
}

