package kubed.demo

import javafx.application.Application
import javafx.geometry.Point2D
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.stage.Stage
import kubed.shape.circle
import kubed.timer.timer
import kubed.util.PoissonDiscSampler

/**
 * Demonstrates Poisson-desc sampling.
 *
 * Based on https://bl.ocks.org/mbostock/19168c663618b7f07158
 */
class PoissonDiscDemo : Application() {
    override fun start(primaryStage: Stage?) {
        val root = Group()

        val width = 800.0
        val height = 400.0

        val sampler = PoissonDiscSampler(width, height, 10.0)

        val c = circle<Point2D> {
            translateX { p, _ -> p.x }
            translateY { p, _ -> p.y }
            radius { _, _ -> 2.0 }
            fill(Color.BLACK)
        }

        timer {
            for(i in 0 until 10) {
                val p = sampler()
                if(p != null) root.children += c(p)
                else {
                    stop()
                    break
                }
            }
        }

        val scene = Scene(root)
        primaryStage?.width = width
        primaryStage?.height = height

        primaryStage?.scene = scene
        primaryStage?.show()
    }

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            launch(PoissonDiscDemo::class.java, *args)
        }
    }
}

